# Generated API client

The frontend does not hand-write HTTP calls. All services and models in `src/app/api/` are generated
from the backend's OpenAPI spec with [ng-openapi-gen](https://github.com/cyclosproject/ng-openapi-gen).

```
Spring controllers + DTOs ──springdoc──▶ /v3/api-docs (OpenAPI JSON) ──ng-openapi-gen──▶ src/app/api/
```

## Regenerating

Run this every time a backend endpoint or DTO changes.

1. Start the backend. It needs JDK 25 and Postgres from `compose.yaml`:
   ```bash
   ./mvnw spring-boot:run
   ```
   Check that `http://localhost:8080/v3/api-docs` returns JSON. Swagger UI is at
   `http://localhost:8080/swagger-ui.html`.
2. Generate the client from `frontend/`:
   ```bash
   npm run generate:api
   ```
3. Commit the changes in `src/app/api/`. The generated code is committed, so the frontend builds
   without a running backend.

Never edit files in `src/app/api/` by hand. The next generation overwrites them, and
`removeStaleFiles` deletes anything the generator did not create.

### Configuration (`ng-openapi-gen.json`)

| Option | Value | Why |
|---|---|---|
| `input` | `http://localhost:8080/v3/api-docs` | Spec served by the running backend |
| `output` | `src/app/api` | Where the code goes |
| `services` | `true` | One injectable service per `@Tag`, e.g. `AuthService` |
| `apiService` | `false` | No generic `Api.invoke(fn, ...)` helper, because we use the services |
| `promises` | `false` | Methods return `Observable`s, which fit `rxMethod` in signal stores |
| `removeStaleFiles` | `true` | Endpoints removed from the backend disappear from the client |

## What gets generated

```
src/app/api/
├── services/          AuthService, CollectionsService, FlashcardsService  ← use these
├── models/            TokenDto, CollectionDto, LoginDto, ...              ← and these
├── fn/                one function per endpoint (used internally by the services)
└── api-configuration.ts, base-service.ts, request-builder.ts, ...         ← plumbing
```

## Using the generated classes

**Services** are `providedIn: 'root'`. Inject them and call them, preferably from a signal store
rather than a component. Every endpoint gets two methods:

```ts
const authService = inject(AuthService);

authService.login({ body: { username, password } });          // Observable<TokenDto> – just the body
authService.login$Response({ body: { username, password } }); // Observable<StrictHttpResponse<TokenDto>> – status, headers
```

Parameters are always passed as one object. `body` holds the request body, and path and query
parameters use their own names:

```ts
collectionsService.getCollections({ collectionFilter: { name: 'animals' }, pageable: { page: 0, size: 20 } });
collectionsService.delete({ id: 42 });
```

Inside a signal store (see `core/auth/auth.store.ts`):

```ts
withMethods((store, authService = inject(AuthService), toastr = inject(ToastrService)) => ({
  login: rxMethod<LoginDto>(
    pipe(
      tap(() => patchState(store, { loading: true })),
      switchMap((credentials) =>
        authService.login({ body: credentials }).pipe(
          tapResponse({
            next: (token) => patchState(store, { /* ... */ loading: false }),
            error: (error: HttpErrorResponse) => {
              patchState(store, { loading: false });
              toastr.error(error.error?.detail ?? 'Something went wrong');
            },
          }),
        ),
      ),
    ),
  ),
}));
```

**Models** are plain interfaces: `import { CollectionDto } from '../../api/models/collection-dto';`.
A field is optional (`name?: string`) unless the backend marks it as required (see `@NotNull` below).

**Path constants** live on each service, e.g. `AuthService.LoginPath`. Use them instead of
hard-coding URLs. The auth interceptor uses `AuthService.LoginPath` to skip the token on login.

**Root URL.** The spec advertises `http://localhost:8080`, but `app.config.ts` sets
`provideApiConfiguration('')`. That keeps requests relative, so `ng serve` sends them through the
dev proxy (`proxy.conf.json`) and you get no CORS errors.

## Backend annotations that shape the spec

What you put on controllers and DTOs decides what the generated TypeScript looks like.

### Used in this project

| Annotation / setting | Where | Effect on the spec and generated code |
|---|---|---|
| `@Tag(name = "Collections")` | Controller class | Groups endpoints. Becomes the service name (`CollectionsService`) and the Swagger UI section. Without it you get `CollectionControllerService`. |
| `@Operation(summary = "...")` | Controller method | Description of the endpoint. Shown in Swagger UI and as the JSDoc on the generated method. |
| `@SecurityRequirements` (empty) | Controller method | Marks the endpoint as public. It overrides the global bearer-JWT requirement from `OpenApiConfiguration`. Used on `/login`. |
| `@NotNull` | DTO record component | Field is required. Generates `id: number` instead of `id?: number`. Request DTOs also get it from `@NotBlank`. |
| `springdoc.default-produces-media-type: application/json` | `application.yaml` | Without it responses are documented as `*/*`, and the generator reads them as `Blob`s instead of JSON. |
| `OpenApiConfiguration` bean | `configuration/` | Title, version and the `bearer-jwt` security scheme. This gives Swagger UI its **Authorize** button. |

Rule of thumb for response DTOs: add `@NotNull` to every field the database guarantees (NOT NULL
column, generated id). Leave truly nullable fields unannotated so TypeScript forces a null check.

### Other useful ones

| Annotation | Example | Effect |
|---|---|---|
| `@Schema(description, example)` | `@Schema(description = "ISO date", example = "2026-10-20T10:00:00") LocalDateTime createdAt` | Docs and example values in Swagger UI and JSDoc |
| `@Schema(requiredMode = REQUIRED)` | on a field | Same as `@NotNull` for the spec, but without bean validation |
| `@Operation(operationId = "...")` | `@Operation(operationId = "listCollections")` | Overrides the generated method name, which is the Java method name by default |
| `@ApiResponse(responseCode, description)` | `@ApiResponse(responseCode = "404", description = "Collection not found")` | Documents error responses in Swagger UI |
| `@Parameter(description, example)` | on a `@PathVariable` / `@RequestParam` | Documents a single parameter |
| `@ParameterObject` | on `Pageable` / a filter object | Expands the object into separate query params (`page`, `size`, `sort`) instead of one nested param |
| `@Hidden` | controller or method | Leaves the endpoint out of the spec and the generated client |

All of these come from `io.swagger.v3.oas.annotations.*`, except `@NotNull` (`jakarta.validation`) and
`@ParameterObject` (`org.springdoc.core.annotations`).
