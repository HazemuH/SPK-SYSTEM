import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Pencil, Plus, Shield, Trash2, User as UserIcon } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select } from "@/components/ui/select";
import { EmptyState, ErrorState, LoadingState } from "@/components/ui/states";
import { getApiErrorMessage } from "@/lib/api-client";
import { useAuth } from "@/features/auth/use-auth";
import { usersApi, type CreateUserInput, type UpdateUserInput, type User } from "./users-api";

// On create, username + password are required. On edit, username is fixed and
// password is an optional reset — so the schema is split per mode below.
const baseSchema = {
  name: z.string().min(1, "Nama wajib diisi"),
  email: z.string().email("Format email tidak valid"),
  role: z.enum(["ADMIN", "USER"]),
};
const createSchema = z.object({
  ...baseSchema,
  username: z.string().min(1, "Username wajib diisi"),
  password: z.string().min(8, "Password minimal 8 karakter"),
});
const editSchema = z.object({
  ...baseSchema,
  password: z.union([z.string().min(8, "Password minimal 8 karakter"), z.literal("")]).optional(),
});

export function UsersPage() {
  const queryClient = useQueryClient();
  const { user: currentUser } = useAuth();
  const [editing, setEditing] = useState<User | null>(null);
  const [creating, setCreating] = useState(false);
  const [deleting, setDeleting] = useState<User | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ["users"],
    queryFn: usersApi.list,
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["users"] });

  const removeMutation = useMutation({
    mutationFn: (id: string) => usersApi.remove(id),
    onSuccess: () => {
      void invalidate();
      setDeleting(null);
      setDeleteError(null);
    },
    onError: (err) => setDeleteError(getApiErrorMessage(err)),
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Pengguna</h1>
          <p className="text-sm text-muted-foreground">Kelola akun admin &amp; staf beserta perannya.</p>
        </div>
        <Button onClick={() => setCreating(true)}>
          <Plus className="h-4 w-4" />
          Tambah Pengguna
        </Button>
      </div>

      {isLoading ? (
        <LoadingState />
      ) : isError ? (
        <ErrorState message={getApiErrorMessage(error)} onRetry={() => void refetch()} />
      ) : data!.length === 0 ? (
        <EmptyState message="Belum ada pengguna." />
      ) : (
        <div className="grid gap-3 sm:grid-cols-2">
          {data!.map((u) => {
            const isAdmin = u.role === "admin";
            const isSelf = currentUser?.id === u.id;
            return (
              <Card key={u.id} className="flex items-center gap-4 p-4">
                <div className="flex h-11 w-11 items-center justify-center rounded-full bg-primary/10 text-sm font-bold text-primary">
                  {u.name.slice(0, 2).toUpperCase()}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="flex items-center gap-2 font-semibold">
                    {u.name}
                    {isSelf && <span className="text-[10px] text-muted-foreground">(Anda)</span>}
                  </p>
                  <p className="truncate text-sm text-muted-foreground">{u.email}</p>
                </div>
                <Badge variant={isAdmin ? "default" : "secondary"} className="capitalize">
                  {isAdmin ? <Shield className="mr-1 h-3 w-3" /> : <UserIcon className="mr-1 h-3 w-3" />}
                  {u.role}
                </Badge>
                <div className="flex gap-1">
                  <Button variant="outline" size="icon" onClick={() => setEditing(u)}>
                    <Pencil className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="outline"
                    size="icon"
                    disabled={isSelf}
                    title={isSelf ? "Tidak dapat menghapus akun sendiri" : "Hapus"}
                    onClick={() => {
                      setDeleteError(null);
                      setDeleting(u);
                    }}
                  >
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {(creating || editing) && (
        <UserDialog
          user={editing}
          onClose={() => {
            setCreating(false);
            setEditing(null);
          }}
          onSaved={() => {
            void invalidate();
            setCreating(false);
            setEditing(null);
          }}
        />
      )}

      <Dialog
        open={!!deleting}
        onClose={() => setDeleting(null)}
        title="Hapus Pengguna?"
        footer={
          <>
            <Button variant="ghost" onClick={() => setDeleting(null)}>
              Batal
            </Button>
            <Button
              variant="destructive"
              disabled={removeMutation.isPending}
              onClick={() => deleting && removeMutation.mutate(deleting.id)}
            >
              Ya, Hapus
            </Button>
          </>
        }
      >
        <p className="text-sm text-muted-foreground">
          Yakin menghapus <span className="font-medium text-foreground">{deleting?.name}</span>?
        </p>
        {deleteError && <p className="mt-3 text-sm text-destructive">{deleteError}</p>}
      </Dialog>
    </div>
  );
}

function UserDialog({
  user,
  onClose,
  onSaved,
}: {
  user: User | null;
  onClose: () => void;
  onSaved: () => void;
}) {
  const isEdit = !!user;
  const [serverError, setServerError] = useState<string | null>(null);

  type CreateValues = z.infer<typeof createSchema>;
  type EditValues = z.infer<typeof editSchema>;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<CreateValues | EditValues>({
    resolver: zodResolver(isEdit ? editSchema : createSchema),
    defaultValues: isEdit
      ? { name: user!.name, email: user!.email, role: user!.role.toUpperCase() as "ADMIN" | "USER", password: "" }
      : { name: "", email: "", username: "", password: "", role: "USER" },
  });

  async function onSubmit(values: CreateValues | EditValues) {
    setServerError(null);
    try {
      if (isEdit) {
        const v = values as EditValues;
        const input: UpdateUserInput = { name: v.name, email: v.email, role: v.role };
        if (v.password && v.password.length > 0) input.password = v.password;
        await usersApi.update(user!.id, input);
      } else {
        const v = values as CreateValues;
        const input: CreateUserInput = {
          username: v.username,
          email: v.email,
          name: v.name,
          password: v.password,
          role: v.role,
        };
        await usersApi.create(input);
      }
      onSaved();
    } catch (err) {
      setServerError(getApiErrorMessage(err));
    }
  }

  return (
    <Dialog
      open
      onClose={onClose}
      title={isEdit ? "Edit Pengguna" : "Tambah Pengguna"}
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Batal
          </Button>
          <Button form="user-form" type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Menyimpan..." : "Simpan"}
          </Button>
        </>
      }
    >
      <form id="user-form" onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        {!isEdit && (
          <div className="space-y-2">
            <Label htmlFor="username">Username</Label>
            <Input id="username" {...register("username")} />
            {"username" in errors && errors.username && (
              <p className="text-xs text-destructive">{errors.username.message as string}</p>
            )}
          </div>
        )}
        <div className="space-y-2">
          <Label htmlFor="name">Nama Lengkap</Label>
          <Input id="name" {...register("name")} />
          {errors.name && <p className="text-xs text-destructive">{errors.name.message as string}</p>}
        </div>
        <div className="space-y-2">
          <Label htmlFor="email">Email</Label>
          <Input id="email" type="email" {...register("email")} />
          {errors.email && <p className="text-xs text-destructive">{errors.email.message as string}</p>}
        </div>
        <div className="space-y-2">
          <Label htmlFor="role">Role</Label>
          <Select id="role" {...register("role")}>
            <option value="ADMIN">Admin</option>
            <option value="USER">User</option>
          </Select>
        </div>
        <div className="space-y-2">
          <Label htmlFor="password">{isEdit ? "Password Baru (opsional)" : "Password"}</Label>
          <Input
            id="password"
            type="password"
            autoComplete="new-password"
            placeholder={isEdit ? "Kosongkan bila tidak diubah" : ""}
            {...register("password")}
          />
          {errors.password && <p className="text-xs text-destructive">{errors.password.message as string}</p>}
        </div>
        {serverError && <p className="text-sm text-destructive">{serverError}</p>}
      </form>
    </Dialog>
  );
}
