#!/usr/bin/env bash
#
# Scaffold a full CRUD feature package so you can vibe-code the domain, not the boilerplate.
#
# Usage:   scripts/new-feature.sh <Entity> [table]
# Example: scripts/new-feature.sh Product            # table/path defaults to "products"
#          scripts/new-feature.sh ToyCriterion criteria
#
# Generates (all compiling, mirroring the existing conventions):
#   <feature>/<Entity>.java            entity (extends BaseEntity: id + timestamps)
#   <feature>/<Entity>Repository.java  Spring Data JPA repository
#   <feature>/<Entity>Service.java     paged list / get / create / update / delete
#   <feature>/<Entity>Controller.java  REST controller under /<table>
#   <feature>/dto/<Entity>Request.java validated request record
#   <feature>/dto/<Entity>Response.java response record + from(entity) mapper
#   db/migration/V<n>__create_<table>.sql  Flyway migration (auto-incremented version)
#   test/.../<Entity>ControllerTest.java   happy + failure path tests
#
# The generated code uses a single placeholder `name` field — rename it / add your
# columns in the entity, the migration, and the DTOs, then run: ./mvnw test
set -euo pipefail

# --- resolve project root (parent of this script's dir) ------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT"

# --- args ----------------------------------------------------------------------
if [[ $# -lt 1 ]]; then
  echo "Usage: scripts/new-feature.sh <Entity> [table]" >&2
  echo "Example: scripts/new-feature.sh Product products" >&2
  exit 1
fi

ENTITY="$1"
if [[ ! "$ENTITY" =~ ^[A-Z][A-Za-z0-9]*$ ]]; then
  echo "Error: <Entity> must be PascalCase, e.g. Product or ToyCriterion (got '$ENTITY')" >&2
  exit 1
fi

PKG="$(printf '%s' "$ENTITY" | tr 'A-Z' 'a-z')"                       # package + folder (all lower)
VAR="$(printf '%s' "${ENTITY:0:1}" | tr 'A-Z' 'a-z')${ENTITY:1}"      # local var (camelCase)
TABLE="${2:-${PKG}s}"                                                 # url path + table (plural)

BASE="src/main/java/com/spkmainan/${PKG}"
TESTBASE="src/test/java/com/spkmainan/${PKG}"

if [[ -d "$BASE" ]]; then
  echo "Error: feature package already exists: $BASE" >&2
  exit 1
fi

# --- next Flyway version -------------------------------------------------------
LAST="$(ls src/main/resources/db/migration 2>/dev/null \
        | sed -n 's/^V\([0-9][0-9]*\)__.*/\1/p' | sort -n | tail -1)"
VERSION=$(( ${LAST:-0} + 1 ))
MIGRATION="src/main/resources/db/migration/V${VERSION}__create_${TABLE}.sql"

mkdir -p "$BASE/dto" "$TESTBASE"

# --- entity --------------------------------------------------------------------
cat > "$BASE/@ENTITY@.java" <<'EOF'
package com.spkmainan.@PKG@;

import com.spkmainan.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "@TABLE@")
public class @ENTITY@ extends BaseEntity {

    // TODO: replace/extend with your real columns. `name` is a placeholder.
    @Column(nullable = false, unique = true)
    private String name;

    protected @ENTITY@() {
        // Required by JPA.
    }

    public @ENTITY@(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
EOF

# --- repository ----------------------------------------------------------------
cat > "$BASE/@ENTITY@Repository.java" <<'EOF'
package com.spkmainan.@PKG@;

import org.springframework.data.jpa.repository.JpaRepository;

public interface @ENTITY@Repository extends JpaRepository<@ENTITY@, Long> {

    boolean existsByNameIgnoreCase(String name);
}
EOF

# --- request dto ---------------------------------------------------------------
cat > "$BASE/dto/@ENTITY@Request.java" <<'EOF'
package com.spkmainan.@PKG@.dto;

import jakarta.validation.constraints.NotBlank;

public record @ENTITY@Request(
        @NotBlank(message = "name is required") String name) {
}
EOF

# --- response dto --------------------------------------------------------------
cat > "$BASE/dto/@ENTITY@Response.java" <<'EOF'
package com.spkmainan.@PKG@.dto;

import com.spkmainan.@PKG@.@ENTITY@;

public record @ENTITY@Response(String id, String name) {

    public static @ENTITY@Response from(@ENTITY@ entity) {
        return new @ENTITY@Response(String.valueOf(entity.getId()), entity.getName());
    }
}
EOF

# --- service -------------------------------------------------------------------
cat > "$BASE/@ENTITY@Service.java" <<'EOF'
package com.spkmainan.@PKG@;

import com.spkmainan.common.dto.PageResponse;
import com.spkmainan.common.exception.ConflictException;
import com.spkmainan.common.exception.ResourceNotFoundException;
import com.spkmainan.@PKG@.dto.@ENTITY@Request;
import com.spkmainan.@PKG@.dto.@ENTITY@Response;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class @ENTITY@Service {

    private final @ENTITY@Repository repository;

    public @ENTITY@Service(@ENTITY@Repository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageResponse<@ENTITY@Response> findAll(Pageable pageable) {
        return PageResponse.from(repository.findAll(pageable).map(@ENTITY@Response::from));
    }

    @Transactional(readOnly = true)
    public @ENTITY@Response findById(Long id) {
        return @ENTITY@Response.from(getOrThrow(id));
    }

    @Transactional
    public @ENTITY@Response create(@ENTITY@Request request) {
        if (repository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("@ENTITY@ already exists: " + request.name());
        }
        @ENTITY@ @VAR@ = new @ENTITY@(request.name());
        return @ENTITY@Response.from(repository.save(@VAR@));
    }

    @Transactional
    public @ENTITY@Response update(Long id, @ENTITY@Request request) {
        @ENTITY@ @VAR@ = getOrThrow(id);
        @VAR@.setName(request.name());
        return @ENTITY@Response.from(repository.save(@VAR@));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    private @ENTITY@ getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("@ENTITY@ not found: " + id));
    }
}
EOF

# --- controller ----------------------------------------------------------------
cat > "$BASE/@ENTITY@Controller.java" <<'EOF'
package com.spkmainan.@PKG@;

import com.spkmainan.common.dto.PageResponse;
import com.spkmainan.@PKG@.dto.@ENTITY@Request;
import com.spkmainan.@PKG@.dto.@ENTITY@Response;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/@TABLE@")
public class @ENTITY@Controller {

    private final @ENTITY@Service service;

    public @ENTITY@Controller(@ENTITY@Service service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<@ENTITY@Response> list(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public @ENTITY@Response get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public @ENTITY@Response create(@Valid @RequestBody @ENTITY@Request request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public @ENTITY@Response update(@PathVariable Long id, @Valid @RequestBody @ENTITY@Request request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
EOF

# --- migration -----------------------------------------------------------------
cat > "$MIGRATION" <<'EOF'
-- @ENTITY@ table. Portable DDL (H2 v2 + PostgreSQL).
-- TODO: add the columns your entity needs; keep them in sync with @ENTITY@.java.
CREATE TABLE @TABLE@ (
    id         BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
EOF

# --- test ----------------------------------------------------------------------
cat > "$TESTBASE/@ENTITY@ControllerTest.java" <<'EOF'
package com.spkmainan.@PKG@;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spkmainan.@PKG@.dto.@ENTITY@Request;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class @ENTITY@ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void create_thenList_returnsItem() throws Exception {
        String body = objectMapper.writeValueAsString(new @ENTITY@Request("Sample @ENTITY@"));

        mockMvc.perform(post("/@TABLE@")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Sample @ENTITY@"));

        mockMvc.perform(get("/@TABLE@"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser
    void create_blankName_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(new @ENTITY@Request(""));

        mockMvc.perform(post("/@TABLE@")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/@TABLE@"))
                .andExpect(status().isUnauthorized());
    }
}
EOF

# --- substitute placeholder tokens & rename @ENTITY@ filenames -----------------
substitute() {
  # macOS/BSD sed in-place
  sed -i '' \
    -e "s/@ENTITY@/${ENTITY}/g" \
    -e "s/@PKG@/${PKG}/g" \
    -e "s/@VAR@/${VAR}/g" \
    -e "s/@TABLE@/${TABLE}/g" \
    "$1"
}

# rename files first (they were written with the literal @ENTITY@ marker), then patch contents
find "$BASE" "$TESTBASE" -name '@ENTITY@*.java' | while read -r f; do
  mv "$f" "${f//@ENTITY@/$ENTITY}"
done

find "$BASE" "$TESTBASE" -name '*.java' -print0 | while IFS= read -r -d '' f; do
  substitute "$f"
done
substitute "$MIGRATION"

# --- done ----------------------------------------------------------------------
cat <<DONE

✅ Generated feature '${ENTITY}' (table: ${TABLE}, migration: V${VERSION})

   ${BASE}/
     ├── ${ENTITY}.java            (entity — rename the 'name' placeholder column)
     ├── ${ENTITY}Repository.java
     ├── ${ENTITY}Service.java     (paged CRUD)
     ├── ${ENTITY}Controller.java  (REST /${TABLE})
     └── dto/${ENTITY}{Request,Response}.java
   ${MIGRATION}
   ${TESTBASE}/${ENTITY}ControllerTest.java

Next:
  1. Edit ${ENTITY}.java, the migration, and the DTOs to use your real fields.
  2. ./mvnw test
  3. Endpoints (auth required): GET/POST /v1/${TABLE}, GET/PUT/DELETE /v1/${TABLE}/{id}
DONE
