# Deploy SPK Mainan on a GCP free-tier VM (e2-micro)

This runs the whole stack (PostgreSQL + Spring Boot API + React web) with Docker Compose
on **one always-free GCP VM**. No cold starts, database never expires, $0 forever.

**What you get:** the app at `http://<VM_EXTERNAL_IP>` (port 80). The API is reachable
only internally (nginx proxies `/v1` → backend), so there is no CORS to configure.

> **Heads-up — 1 GB RAM.** The e2-micro has only 1 GB. We add a 2 GB swap file and cap
> every container (see `docker-compose.prod.yml`). Building images on the VM is slow
> (~10–15 min) but works. Follow the steps in order.

---

## 1. Create the VM (GCP Console — one time)

1. Go to **console.cloud.google.com** → create/select a project (billing must be enabled,
   but the e2-micro below stays within the always-free tier — you won't be charged for it).
2. **Compute Engine → VM instances → Create instance**. Set:
   - **Name:** `spk-mainan`
   - **Region:** must be one of the always-free regions — **`us-west1`**, **`us-central1`**,
     or **`us-east1`**. (Any other region is NOT free.)
   - **Machine type:** `e2-micro` (under the "E2" series).
   - **Boot disk:** Ubuntu 22.04 LTS, **30 GB** Standard persistent disk (free tier limit).
   - **Firewall:** check **Allow HTTP traffic**. (Leave HTTPS unchecked unless you add a domain.)
3. Click **Create**. Note the **External IP** shown in the instances list.

---

## 2. Connect and install Docker

Click the **SSH** button next to the instance in the console (opens a browser terminal —
no key setup needed). Then paste these blocks one at a time.

**Add a 2 GB swap file** (essential — prevents out-of-memory crashes on 1 GB):

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
free -h   # confirm you now see ~2.0Gi of swap
```

**Install Docker + the Compose plugin:**

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl git
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker $USER
```

Then **log out and back in** (close the SSH tab, reopen it) so the `docker` group applies.
Verify: `docker ps` should run without `sudo`.

---

## 3. Get the code and set secrets

```bash
git clone https://github.com/HazemuH/SPK-SYSTEM.git
cd SPK-SYSTEM
```

Create the `.env` with **real** secrets (do not reuse the examples):

```bash
cat > .env <<EOF
DB_PASSWORD=$(openssl rand -hex 16)
JWT_SECRET=$(openssl rand -hex 32)
CORS_ALLOWED_ORIGINS=*
EOF
cat .env   # note these down somewhere safe
```

> `CORS_ALLOWED_ORIGINS=*` is harmless here because the browser talks to the app
> same-origin (nginx proxies `/v1`); the API is not exposed publicly on its own port.

---

## 4. Build and start

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

First run builds all three images — **be patient, ~10–15 min** on 1 GB. Watch progress:

```bash
docker compose logs -f backend   # Ctrl-C to stop watching (containers keep running)
```

Wait until you see Spring Boot's `Started SpkMainanApplication`. Then check health:

```bash
curl -s http://localhost/v1/actuator/health   # expect {"status":"UP"}
```

---

## 5. Open it

In your browser: **`http://<VM_EXTERNAL_IP>`** — log in with `admin` / `password123`.

That's it — the app is live and always-on.

---

## Everyday operations

| Task | Command (run inside `~/SPK-SYSTEM`) |
|------|-------------------------------------|
| See running containers | `docker compose ps` |
| View logs | `docker compose logs -f backend` |
| Restart everything | `docker compose -f docker-compose.yml -f docker-compose.prod.yml restart` |
| Stop everything | `docker compose down` |
| Deploy new code | `git pull && docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build` |
| Check memory/swap | `free -h` |

The Postgres data lives in a Docker volume (`db-data`) and survives restarts. Back it up with:

```bash
docker compose exec db pg_dump -U spk_mainan spk_mainan > backup_$(date +%F).sql
```

---

## Troubleshooting

- **Build gets "Killed" / hangs:** the VM ran out of memory. Confirm swap is on (`free -h`
  shows ~2 GB swap). If a build still OOMs, build the backend alone first:
  `docker compose -f docker-compose.yml -f docker-compose.prod.yml build backend`, then bring up the rest.
- **`curl ... /health` refuses connection:** backend still starting — watch
  `docker compose logs -f backend` for `Started SpkMainanApplication`.
- **Site loads but login fails with a network error:** check the backend is UP
  (`docker compose ps`) and healthy (`curl http://localhost/v1/actuator/health`).
- **Can't reach `http://<IP>` at all:** confirm the VM has **Allow HTTP traffic** enabled
  (Console → the instance → Edit → Firewalls), and you're using `http://` not `https://`.

## Optional: a real domain + HTTPS

Point a free domain (e.g. from DuckDNS) at the VM's IP, then swap the frontend for a
Caddy reverse proxy to get automatic Let's Encrypt HTTPS. Ask and I'll set that up — it's
an extra layer beyond this guide.
