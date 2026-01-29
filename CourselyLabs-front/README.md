# CourselyLabs Frontend

## Project Setup

```sh
npm install
```

### Development

```sh
npm run dev
```

### Build for Production

```sh
npm run build
```

### Run Unit Tests with [Vitest](https://vitest.dev/)

```sh
npm run test:unit
```

### Lint with [ESLint](https://eslint.org/)

```sh
npm run lint
```

---

## Environment Variables

Vite exposes variables prefixed with `VITE_` to the client code via `import.meta.env`.

| Variable             | Description          | Default (dev)            |
|----------------------|----------------------|--------------------------|
| `VITE_API_BASE_URL`  | Backend API base URL | `http://localhost:8080`  |

### Files

| File               | Purpose                                    | Committed to git? |
|--------------------|--------------------------------------------|--------------------|
| `.env`             | Default values for all environments        | Yes                |
| `.env.production`  | Overrides for `npm run build`              | Yes                |
| `.env.local`       | Local overrides (personal machine config)  | No                 |

### Usage in code

```ts
const { data } = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/courses/all`)
```

### Adding new variables

1. Add the variable to `.env` with a default value
2. Add the production value to `.env.production`
3. Access it in code with `import.meta.env.VITE_YOUR_VARIABLE`

---

## How to Create a Page That Fetches Backend Data

### 1. Create the View Component

Create a new `.vue` file in `src/views/`. Use `axios` to call the backend API and Quasar components for the UI.

```vue
<!-- src/views/ExampleView.vue -->
<template>
  <div style="padding: 2rem;">
    <h4>Title</h4>

    <q-spinner v-if="loading" size="3em" color="primary" />

    <q-banner v-else-if="error" class="bg-negative text-white q-mb-md">
      {{ error }}
    </q-banner>

    <div v-else>
      <!-- Use Quasar components to display data -->
      <q-list bordered separator>
        <q-item v-for="item in items" :key="item.id">
          <q-item-section>
            <q-item-label>{{ item.name }}</q-item-label>
            <q-item-label caption>{{ item.description }}</q-item-label>
          </q-item-section>
        </q-item>
      </q-list>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

interface Item {
  id: number
  name: string
  description: string
}

const items = ref<Item[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const { data } = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/your-endpoint`)
    items.value = data
  } catch (e) {
    error.value = 'Error loading data.'
  } finally {
    loading.value = false
  }
})
</script>
```

**Key points:**

- Use a plain `<div>` as root element (not `<q-page>`, which requires a full Quasar layout).
- Define a TypeScript `interface` matching the backend DTO fields.
- Use `ref()` for reactive state and `onMounted()` to fetch on page load.
- Handle loading and error states.

### 2. Add a Route

Register the view in `src/router/index.ts`:

```ts
{
  path: '/example',
  name: 'example',
  component: () => import('../views/ExampleView.vue'),
},
```

### 3. Add a Navigation Link

Add a `<RouterLink>` in `src/App.vue` inside the `<nav>`:

```html
<RouterLink to="/example">Example</RouterLink>
```

### 4. Run It

Make sure all three services are running:

```bash
# 1. Database (from project root)
docker compose up -d

# 2. Backend (from CourselyLabs-back/)
./mvnw spring-boot:run

# 3. Frontend (from CourselyLabs-front/)
npm run dev
```

Then open `http://localhost:5173/example`.

---

## Available Backend Endpoints

| Resource    | Endpoint                          | Method | Description          |
|-------------|-----------------------------------|--------|----------------------|
| Courses     | `/api/courses/all`                | GET    | All courses          |
| Courses     | `/api/courses/{id}`               | GET    | Course by ID         |
| Courses     | `/api/courses/search?keyword=...` | GET    | Search courses       |
| Categories  | `/api/categories`                 | GET    | All categories       |
| Users       | `/api/users`                      | GET    | All users            |
| Users       | `/api/users/{id}`                 | GET    | User by ID           |
| Enrollments | `/api/enrollments/user/{userId}`  | GET    | User's enrollments   |
| Reviews     | `/api/reviews/course/{courseId}`   | GET    | Reviews for a course |

## Common Quasar Components

| Component   | Use for                |
|-------------|------------------------|
| `q-card`    | Content cards          |
| `q-list`    | Lists of items         |
| `q-table`   | Data tables with pagination |
| `q-spinner` | Loading indicators     |
| `q-banner`  | Error/info messages    |
| `q-badge`   | Labels and tags        |
| `q-btn`     | Buttons                |
| `q-input`   | Form inputs            |

Full component docs: https://quasar.dev/vue-components
