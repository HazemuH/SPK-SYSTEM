#!/usr/bin/env bash
#
# Scaffold a management page (list table) so you vibe-code the columns, not the wiring.
#
# Usage:   scripts/new-page.sh <Type> <route>
# Example: scripts/new-page.sh Criterion criteria
#          scripts/new-page.sh Category categories
#
# Generates under src/pages/<route>/:
#   <route>-types.ts   the <Type> interface
#   <route>-api.ts     data access (mock now; swap to apiClient later)
#   <route>-page.tsx   <Route>Page — a searchable table with loading/error/empty states
#
# Then it prints the 3 lines to register the page (paths / nav / router).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT"

if [[ $# -lt 2 ]]; then
  echo "Usage: scripts/new-page.sh <Type> <route>" >&2
  echo "Example: scripts/new-page.sh Criterion criteria" >&2
  exit 1
fi

TYPE="$1"     # PascalCase singular, e.g. Criterion
ROUTE="$2"    # plural lowercase, e.g. criteria

if [[ ! "$TYPE" =~ ^[A-Z][A-Za-z0-9]*$ ]]; then
  echo "Error: <Type> must be PascalCase (got '$TYPE')" >&2; exit 1
fi
if [[ ! "$ROUTE" =~ ^[a-z][a-z0-9-]*$ ]]; then
  echo "Error: <route> must be lowercase (got '$ROUTE')" >&2; exit 1
fi

# Component/prefix names
COMPONENT="$(printf '%s' "${ROUTE:0:1}" | tr 'a-z' 'A-Z')${ROUTE:1}"   # criteria -> Criteria
DIR="src/pages/${ROUTE}"

if [[ -d "$DIR" ]]; then
  echo "Error: page already exists: $DIR" >&2; exit 1
fi
mkdir -p "$DIR"

# --- types ---------------------------------------------------------------------
cat > "$DIR/@ROUTE@-types.ts" <<'EOF'
/** A @TYPE@ record. Add your real fields here. */
export interface @TYPE@ {
  id: string;
  name: string;
}
EOF

# --- api (mock) ----------------------------------------------------------------
cat > "$DIR/@ROUTE@-api.ts" <<'EOF'
import type { @TYPE@ } from "./@ROUTE@-types";
// import { apiClient } from "@/lib/api-client";
// import type { PageResponse } from "@/lib/types";

/**
 * Data access for @ROUTE@. MOCK for now.
 * ➜ Swap to the real API once the backend exposes `/@ROUTE@`:
 *   async list(): Promise<@TYPE@[]> {
 *     const { data } = await apiClient.get<PageResponse<@TYPE@>>("/@ROUTE@");
 *     return data.content;
 *   }
 */
const MOCK: @TYPE@[] = [
  { id: "1", name: "Contoh 1" },
  { id: "2", name: "Contoh 2" },
];

export const @ROUTE@Api = {
  async list(): Promise<@TYPE@[]> {
    await new Promise((resolve) => setTimeout(resolve, 300));
    return MOCK;
  },
};
EOF

# --- page ----------------------------------------------------------------------
cat > "$DIR/@ROUTE@-page.tsx" <<'EOF'
import { useQuery } from "@tanstack/react-query";
import { Plus, Search } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { EmptyState, ErrorState, LoadingState } from "@/components/ui/states";
import { getApiErrorMessage } from "@/lib/api-client";
import { @ROUTE@Api } from "./@ROUTE@-api";

export function @COMPONENT@Page() {
  const [search, setSearch] = useState("");

  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ["@ROUTE@"],
    queryFn: @ROUTE@Api.list,
  });

  const rows = (data ?? []).filter((r) =>
    r.name.toLowerCase().includes(search.toLowerCase()),
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">@COMPONENT@</h1>
          <p className="text-sm text-muted-foreground">Kelola data @ROUTE@.</p>
        </div>
        <Button>
          <Plus className="h-4 w-4" />
          Tambah
        </Button>
      </div>

      <div className="relative max-w-xs">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          className="pl-9"
          placeholder="Cari..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {isLoading ? (
        <LoadingState />
      ) : isError ? (
        <ErrorState message={getApiErrorMessage(error)} onRetry={() => void refetch()} />
      ) : rows.length === 0 ? (
        <EmptyState />
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nama</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {rows.map((row) => (
              <TableRow key={row.id}>
                <TableCell className="font-medium">{row.name}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
EOF

# --- substitute tokens & rename ------------------------------------------------
for f in "$DIR"/@ROUTE@-*; do
  mv "$f" "${f//@ROUTE@/$ROUTE}"
done
for f in "$DIR"/*.ts "$DIR"/*.tsx; do
  sed -i '' \
    -e "s/@TYPE@/${TYPE}/g" \
    -e "s/@ROUTE@/${ROUTE}/g" \
    -e "s/@COMPONENT@/${COMPONENT}/g" \
    "$f"
done

cat <<DONE

✅ Generated page '${COMPONENT}' at ${DIR}

Register it in 3 places:

  1) src/routes/paths.ts        → add:  ${ROUTE}: "/${ROUTE}",
  2) src/components/layout/nav-config.tsx → add a NavItem:
        { label: "${COMPONENT}", to: paths.${ROUTE}, icon: SomeIcon },
  3) src/routes/router.tsx      → import { ${COMPONENT}Page } and add a child route:
        { path: paths.${ROUTE}, element: <${COMPONENT}Page /> },

Then: npm run dev
DONE
