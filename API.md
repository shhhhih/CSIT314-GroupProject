# IDP Platform – API Contract

Single source of truth between backend and frontend.
**If you want to change an endpoint, edit this file in a PR and tell the group first.**

Owner: B1 (maintains this file). Status marks: [DONE] = already in `main`, [TO DO] = not built yet.

---

## 0. Global rules

| Topic | Rule |
|---|---|
| Base URL | `http://localhost:8080` |
| Data format | JSON (`Content-Type: application/json`), except file uploads (`multipart/form-data`) |
| IDs | `Long` (numbers), never hard-coded in code |
| Dates | ISO-8601 text, e.g. `"2026-10-21T14:30:00"` (date only: `"2026-10-21"`) |
| Money | number, e.g. `45000.00` |
| Who is calling | Taken **only from the session** (`userId`, `profileName`). The browser never sends a userId |
| Roles (`profileName`) | `User Admin`, `Interior Designer`, `Customer`, `Platform Manager` |
| Account status | `ACTIVE`, `DISABLED` |
| Profile status | `ACTIVE`, `SUSPENDED` |
| Property type | `HDB`, `Condo`, `Landed` (exact text) |
| Project status | `Current`, `Past` (exact text) |

### Error format (every error uses this)
```json
{ "message": "Human readable text shown on the page" }
```

| Status | Meaning | Frontend should |
|---|---|---|
| 400 | Invalid / missing input | show `message` under the form |
| 401 | Not logged in | redirect to `/login.html` |
| 403 | Logged in but not allowed (wrong role, inactive account) | show `message` |
| 404 | Record not found | show `message` |
| 409 | Conflict (duplicate, or still in use) | show `message` |

### Role access matrix

| Endpoint group | User Admin | Interior Designer | Customer | Platform Manager |
|---|---|---|---|---|
| Auth (login, logout, me) | ✔ | ✔ | ✔ | ✔ |
| `/api/profile` (my account) | ✔ | ✔ | ✔ | ✔ |
| `/api/admin/profiles` | ✔ | ✘ | ✘ | ✘ |
| `/api/company` | ✘ | ✔ | ✘ | ✘ |
| `GET /api/categories` | ✔ | ✔ | ✔ | ✔ |
| `POST/PUT/DELETE /api/categories` | ✘ | ✘ | ✘ | ✔ |
| `/api/designer/idps`, `POST /api/idps` | ✘ | ✔ | ✘ | ✘ |
| `GET /api/idps`, `GET /api/idps/{id}` | ✘ | ✘ | ✔ | ✘ |
| `/api/favourites` | ✘ | ✘ | ✔ | ✘ |
| `/api/reports/daily` | ✘ | ✘ | ✘ | ✔ |

Wrong role → `403 {"message":"You do not have permission to do this"}`.

### Test accounts (created by seeders, password `Password123`)
`admin1`, `designer1`, `customer1`, `platform1`, `designer001`–`designer100`, `customer001`–`customer100`.
Inactive tests: `disabled1` (account DISABLED), `suspended1` (profile SUSPENDED).

---

## 1. Authentication (US3 [DONE], US4 [DONE]) – owner B1 / F1

### POST `/api/auth/login` [DONE]
Request
```json
{ "usernameOrEmail": "customer1", "password": "Password123" }
```
200
```json
{ "userId": 3, "username": "customer1", "profileName": "Customer", "redirectUrl": "/customer-home.html" }
```
Errors
- 400 `Please enter your username and password`
- 401 `Invalid username or password`
- 403 `Your account is not active. Please contact the administrator.`

### POST `/api/auth/logout` [DONE] (US4)
No body. Ends the session.
200
```json
{ "message": "Logged out" }
```
Works even if already logged out (still 200). Frontend then goes to `/login.html`.

### GET `/api/auth/me` [DONE]
Lets any page know who is logged in (for navbar and role checks).
200
```json
{ "userId": 3, "username": "customer1", "profileName": "Customer" }
```
401 `Please log in first`

---

## 2. My account ([DONE] in PR #3) – owner existing

### GET `/api/profile`
200
```json
{ "userId": 3, "username": "customer1", "email": "customer1@test.com",
  "role": "Customer", "accountStatus": "ACTIVE", "createdAt": "2026-08-01T09:00:00" }
```
Errors: 401, 403 `This account is not active`, 404 `Account not found`

### PUT `/api/profile`
Request (username 3–50 chars, valid email)
```json
{ "username": "newname", "email": "new@test.com" }
```
200: same body as GET.
Errors: 400 `Enter a valid username and email address` / `Username is already taken` / `Email is already registered`, 401, 403.

---

## 3. User profile management – US2 [TO DO] (User Admin only) – owner B1 / F1

A "user profile" is the role type (see `user_profiles` table).

Profile object
```json
{ "profileId": 2, "profileName": "Interior Designer", "description": "Designs and publishes IDPs",
  "profileStatus": "ACTIVE", "createdAt": "2026-08-01T09:00:00" }
```

| Task | Method + URL | Request | Success |
|---|---|---|---|
| Create | `POST /api/admin/profiles` | `{ "profileName": "...", "description": "..." }` | 201 profile object |
| View one | `GET /api/admin/profiles/{profileId}` | – | 200 profile object |
| List + search | `GET /api/admin/profiles?keyword=` | keyword optional (matches name or description) | 200 array of profile objects (`[]` if none) |
| Update | `PUT /api/admin/profiles/{profileId}` | `{ "profileName": "...", "description": "..." }` | 200 profile object |
| Suspend / activate | `PUT /api/admin/profiles/{profileId}/status` | `{ "profileStatus": "SUSPENDED" }` or `"ACTIVE"` | 200 profile object |

Errors
- 400 `Profile name is required` / `Invalid profile status`
- 403 no permission
- 404 `Profile not found`
- 409 `Profile name already exists`

Rule: accounts whose profile is `SUSPENDED` cannot log in (already enforced by login).

---

## 4. Company information – US5 [TO DO] (Interior Designer) – owner B2 / F1

One company per designer. Company object
```json
{ "companyId": 1, "userId": 12, "companyName": "Cozy Interiors", "description": "We design homes",
  "logo": "/uploads/logos/12.png", "email": "hello@cozy.com", "phone": "+65 9123 4567",
  "address": "1 Orchard Road", "website": "https://cozy.com",
  "createdAt": "2026-10-01T10:00:00", "updatedAt": "2026-10-02T11:00:00" }
```
`logo`, `phone`, `address`, `website` may be `null`.

| Task | Method + URL | Request | Success |
|---|---|---|---|
| View my company (page) | `GET /api/company` | – | 200 company object |
| Save (first time) | `POST /api/company` | `{ companyName, description, email, phone, address, website }` | 201 company object |
| Update | `PUT /api/company` | same fields | 200 company object |
| Upload logo | `POST /api/company/logo` | `multipart/form-data`, field `logo` (png/jpg, max 2 MB) | 200 `{ "logo": "/uploads/logos/12.png" }` |

Errors
- `GET` with no company yet → 404 `No company information yet` (frontend shows the empty form)
- 400 `Company name is required` / `Enter a valid email address` / `Logo must be a PNG or JPG under 2 MB`
- 409 `Company information already exists` (when `POST` is called twice)

---

## 5. IDP categories – US21 [TO DO] – owner B2 / F2

Category object
```json
{ "categoryId": 1, "categoryName": "Living Room", "description": "Living room designs", "createdAt": "2026-08-01T09:00:00" }
```

| Task | Method + URL | Request | Success | Who |
|---|---|---|---|---|
| View + search list | `GET /api/categories?keyword=` | keyword optional | 200 array (`[]` if none) | any logged-in role |
| Create | `POST /api/categories` | `{ "categoryName": "...", "description": "..." }` | 201 category object | Platform Manager |
| Update | `PUT /api/categories/{categoryId}` | same fields | 200 category object | Platform Manager |
| Delete | `DELETE /api/categories/{categoryId}` | – | 200 `{ "message": "Category deleted" }` | Platform Manager |

Errors
- 400 `Category name is required`
- 404 `Category not found`
- 409 `Category name already exists`
- 409 `Category is used by existing IDPs and cannot be deleted`

---

## 6. IDPs for designers – US6, US7 [TO DO] (Interior Designer) – owner B3 / F1

### Objects
IDP summary (used in lists)
```json
{ "idpId": 5, "title": "Modern 4-room HDB", "categoryId": 1, "categoryName": "Living Room",
  "propertyType": "HDB", "projectStatus": "Past", "budget": 45000.00,
  "coverImage": "/uploads/idps/5/1.jpg", "viewCount": 12, "shortlistCount": 3,
  "createdAt": "2026-10-01T10:00:00" }
```
`coverImage` is `null` if the IDP has no image.

IDP details (summary fields plus)
```json
{ "...all summary fields...",
  "description": "Full text description",
  "companyId": 1, "companyName": "Cozy Interiors",
  "images": ["/uploads/idps/5/1.jpg", "/uploads/idps/5/2.jpg"],
  "updatedAt": "2026-10-02T11:00:00" }
```

### POST `/api/idps` – create IDP (US6)
Designer must have saved company information first.
Request
```json
{ "title": "Modern 4-room HDB", "description": "…", "budget": 45000.00,
  "propertyType": "HDB", "projectStatus": "Past", "categoryId": 1 }
```
201: IDP details object (with empty `images`).
Errors
- 400 `Title is required` / `Description is required` / `Budget must be a number greater than 0` / `Select a valid property type` / `Select a valid project status`
- 400 `Select a category`
- 404 `Category not found`
- 409 `Please save your company information first`

### POST `/api/idps/{idpId}/images` – upload images (US6)
`multipart/form-data`, field `images` (one or more, png/jpg, each max 5 MB, max 10 per IDP).
200 `{ "images": ["/uploads/idps/5/1.jpg", "..."] }`
Errors: 400 `Images must be PNG or JPG under 5 MB`, 403 not your IDP, 404 `IDP not found`.
(Frontend flow: call `POST /api/idps`, then call this endpoint with the returned `idpId`.)

### GET `/api/designer/idps?sort=date&order=desc` – my IDP list (US7)
- `sort`: `date` (default) or `title`
- `order`: `asc` or `desc` (default `desc`)
200 array of IDP summaries, only the logged-in designer's own IDPs (`[]` if none).
Error: 400 `Invalid sort option`.

### GET `/api/designer/idps/{idpId}` – my IDP details (US7)
200 IDP details object. Does **not** increase view count.
Errors: 403 `This IDP does not belong to you`, 404 `IDP not found`.

---

## 7. IDPs for customers – US13, US15 [TO DO] (Customer) – owner B4 / F2

### GET `/api/idps?keyword=` – search IDPs (US13)
- `keyword` optional. Matches title, description or category name (case-insensitive, partial match).
- Blank or missing keyword returns all IDPs, newest first.
200 array of IDP summaries. `[]` when nothing matches → frontend shows **"No projects found"**.

### GET `/api/idps/{idpId}` – view IDP details (US15)
200 IDP details object (includes category, images and company name).
Side effect: increases `viewCount` by 1 and stores one view record with date and time (`idp_views`: viewId, idpId, customerId, viewedAt), because the daily report counts views by date.
Errors: 404 `IDP not found`.

---

## 8. Favourites – US17, US18 [TO DO] (Customer) – owner B4 / F2

Favourite item (list row)
```json
{ "favouriteId": 9, "savedAt": "2026-10-03T08:00:00", "idp": { "...IDP summary object..." } }
```

### POST `/api/favourites` – save to favourites (US17)
Request `{ "idpId": 5 }`
201
```json
{ "favouriteId": 9, "idpId": 5, "savedAt": "2026-10-03T08:00:00" }
```
Side effects: `shortlistCount` of that IDP +1; the favourite record keeps `savedAt` (the daily report counts shortlists by date).
Errors: 400 `IDP id is required`, 404 `IDP not found`, 409 `You already saved this IDP`.

### GET `/api/favourites` – my favourites (US18)
200 array of favourite items, newest first (`[]` if none → frontend shows "You have no favourites yet").
"Open saved IDP details" uses `GET /api/idps/{idpId}` from section 7.

---

## 9. Daily report – US22 [TO DO] (Platform Manager) – owner B2 / F2

### GET `/api/reports/daily?date=2026-10-21`
- `date` optional, format `yyyy-MM-dd`, default today.
200
```json
{ "date": "2026-10-21",
  "newUsers": 4, "newIdps": 6, "totalViews": 120, "totalShortlists": 15,
  "topIdps": [ { "idpId": 5, "title": "Modern 4-room HDB", "views": 30 } ] }
```
Definitions (all for that one calendar day, using the stored date/time):
- `newUsers` = user accounts with `createdAt` on that day
- `newIdps` = IDPs with `createdAt` on that day
- `totalViews` = rows in `idp_views` with `viewedAt` on that day
- `totalShortlists` = rows in `favourites` with `savedAt` on that day
- `topIdps` = up to 5 IDPs with the most views that day, highest first (`[]` if no views)

Errors: 400 `Date must be in yyyy-MM-dd format`, 403 no permission.

---

## 10. File uploads

- Saved on disk under the `uploads/` folder in the project root and served at `/uploads/**`.
- `uploads/` must be listed in `.gitignore` (do not commit images).
- Config (B1 adds once): `spring.servlet.multipart.max-file-size=5MB`, `spring.servlet.multipart.max-request-size=50MB`.

---

## 11. Who builds what

| US | Feature | Backend | Frontend |
|---|---|---|---|
| US3 | Login | done | done |
| US4 | Logout | B1 | F1 |
| US2 | Manage user profiles | B1 | F1 |
| US5 | Company information | B2 | F1 |
| US21 | Manage categories | B2 | F2 |
| US22 | Daily report | B2 | F2 |
| US6 | Create IDP | B3 | F1 |
| US7 | View my IDPs | B3 | F1 |
| US13 | Search IDPs | B4 | F2 |
| US15 | View IDP details | B4 | F2 |
| US17 | Save to favourites | B4 | F2 |
| US18 | View favourites | B4 | F2 |

Backend order: Categories (US21) → Company (US5) → IDP (US6, US7) → search/details (US13, US15) → favourites (US17, US18) → daily report (US22).

## 12. Changing this file
1. Open a PR that edits `API.md` (title: `API: <what changed>`).
2. Post in the group chat which endpoint changed.
3. Backend and frontend update their code to match before merging anything that depends on it.
