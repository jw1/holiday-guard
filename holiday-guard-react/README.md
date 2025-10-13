# Holiday Guard React UI

React-based management interface for Holiday Guard. Provides schedule management, calendar visualization, and dashboard views.

## Overview

This is a React single-page application (SPA) that communicates with the Holiday Guard REST API. It provides an intuitive interface for managing schedules, viewing calendar data, and monitoring today's schedule status.

## Technology Stack

- **React 18** - UI framework
- **TanStack Query (React Query)** - Server state management and caching
- **React Router** - Client-side routing
- **Vite** - Build tool and dev server
- **TypeScript** - Type-safe JavaScript
- **CSS Modules** - Scoped component styling

## Project Structure

```
src/
├── components/          # React components
│   ├── Dashboard.tsx           # Today's status dashboard
│   ├── ScheduleViewer.tsx      # Multi-schedule calendar
│   ├── ScheduleList.tsx        # List of all schedules
│   └── ScheduleForm.tsx        # Create/edit schedules
├── api/                # API client functions
│   ├── backend.ts              # Type-safe API calls
│   ├── schedules.ts            # Schedule CRUD operations
│   └── calendar-view.ts        # Calendar data fetching
├── types/              # TypeScript type definitions
│   ├── backend.ts              # Backend DTO types
│   └── calendar-view.ts        # Calendar view types
├── hooks/              # Custom React hooks
│   └── useScheduleQuery.ts     # TanStack Query hooks
├── App.tsx             # Main application component
├── main.tsx            # Application entry point
└── index.css           # Global styles
```

## Key Features

### Dashboard
**Component:** `Dashboard.tsx`
**Route:** `/dashboard`

Displays today's run status for all active schedules.

**Features:**
- Real-time schedule status (RUN, SKIP, FORCE_RUN, FORCE_SKIP)
- Color-coded status indicators
- Reason for each schedule's status
- Automatic refresh on data changes

**Data Source:** `GET /api/v1/dashboard/schedule-status`

### Schedule Viewer (Calendar)
**Component:** `ScheduleViewer.tsx`
**Route:** `/calendar`

Multi-schedule calendar view showing which days schedules should run.

**Features:**
- Month-by-month calendar view
- Multiple schedule overlay
- Color-coded run/skip indicators
- Deviation highlighting
- Month navigation (previous/next)
- Schedule selection/deselection

**Data Source:** `GET /api/v1/calendar-view?yearMonth={ym}&scheduleIds={ids}`

**Optimization:** Uses normalized API response structure for 70% smaller payloads.

### Schedule List
**Component:** `ScheduleList.tsx`
**Route:** `/schedules`

List of all schedules with CRUD operations.

**Features:**
- Table view of all schedules
- Name, description, country, active status
- Edit/delete actions (admin only)
- Create new schedule button
- Real-time updates via React Query

**Data Source:** `GET /api/v1/schedules`

### Schedule Form
**Component:** `ScheduleForm.tsx`
**Route:** `/schedules/new` or `/schedules/{id}/edit`

Form for creating or editing schedules.

**Features:**
- Name and description fields
- Country selector
- Active status toggle
- Validation and error handling
- Submit/cancel actions

**Data Sources:**
- `POST /api/v1/schedules` (create)
- `PUT /api/v1/schedules/{id}` (update)

## State Management with TanStack Query

The app uses TanStack Query (React Query) for server state management, providing:
- **Automatic caching** - Reduces redundant API calls
- **Background refetching** - Keeps data fresh
- **Optimistic updates** - Immediate UI feedback
- **Error handling** - Automatic retry and error boundaries
- **Loading states** - Built-in loading indicators

**Example Query Hook:**
```typescript
export function useSchedules() {
  return useQuery({
    queryKey: ['schedules'],
    queryFn: () => fetchSchedules(),
    staleTime: 30000, // Consider data fresh for 30 seconds
  });
}
```

**Example Mutation Hook:**
```typescript
export function useCreateSchedule() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateScheduleRequest) => createSchedule(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['schedules'] });
    },
  });
}
```

## API Client (backend.ts)

Type-safe API client with TypeScript interfaces matching backend DTOs.

**Key Functions:**
```typescript
// Schedule queries
export async function fetchSchedules(): Promise<Schedule[]>
export async function fetchScheduleById(id: number): Promise<Schedule>

// shouldRun queries
export async function queryShouldRun(scheduleId: number, date?: string): Promise<ShouldRunQueryResponse>

// Calendar views
export async function fetchCalendarView(yearMonth: string, scheduleIds: number[]): Promise<MultiScheduleCalendarView>

// Dashboard
export async function fetchDashboardStatus(): Promise<ScheduleDashboardView[]>
```

**Error Handling:**
```typescript
if (!response.ok) {
  const error = await response.json();
  throw new Error(error.message || 'API request failed');
}
```

## TypeScript Types

### Backend DTOs
**File:** `types/backend.ts`

Mirrors Java backend DTOs for type safety across the stack:

```typescript
export interface Schedule {
  id: number;
  name: string;
  description?: string;
  country: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ShouldRunQueryResponse {
  scheduleId: number;
  queryDate: string;
  shouldRun: boolean;
  runStatus: RunStatus;
  reason: string;
  deviationApplied: boolean;
  versionId: number;
}

export type RunStatus = 'RUN' | 'SKIP' | 'FORCE_RUN' | 'FORCE_SKIP';
```

### Calendar View Types
**File:** `types/calendar-view.ts`

Normalized structure for efficient data transfer:

```typescript
export interface MultiScheduleCalendarView {
  yearMonth: string;
  schedules: ScheduleMonthView[];
}

export interface ScheduleMonthView {
  scheduleId: number;
  scheduleName: string;
  yearMonth: string;
  days: DayStatusView[];
}

export interface DayStatusView {
  date: string;  // ISO format: YYYY-MM-DD
  status: RunStatus;
  reason?: string;
}
```

## Routing

**Library:** React Router v6

**Routes:**
```typescript
<Routes>
  <Route path="/" element={<Dashboard />} />
  <Route path="/dashboard" element={<Dashboard />} />
  <Route path="/calendar" element={<ScheduleViewer />} />
  <Route path="/schedules" element={<ScheduleList />} />
  <Route path="/schedules/new" element={<ScheduleForm mode="create" />} />
  <Route path="/schedules/:id/edit" element={<ScheduleForm mode="edit" />} />
</Routes>
```

## Development

### Prerequisites
- Node.js 18+ and npm
- Holiday Guard backend running (default: http://localhost:8080)

### Available Scripts

```bash
# Install dependencies
npm install

# Start development server (http://localhost:3000)
npm start

# Run tests
npm test

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint
```

### Development Server
```bash
npm start
```
- Opens [http://localhost:3000](http://localhost:3000) in browser
- Hot module replacement (HMR) enabled
- Proxy API requests to backend on port 8080

### Build for Production
```bash
npm run build
```
- Creates optimized production build in `build/` directory
- Minified and bundled JavaScript/CSS
- Ready for deployment

## API Proxy Configuration

During development, the app proxies API requests to avoid CORS issues:

**Vite config:**
```typescript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

In production, the React build is served by the Spring Boot app, eliminating the need for CORS.

## Authentication

The UI uses HTTP Basic Auth for API requests (development) or integrates with OAuth/SSO (production).

**Login Flow:**
1. User provides credentials
2. Browser caches credentials
3. All API requests include `Authorization: Basic {credentials}` header

**Roles:**
- **USER** - Read-only access (dashboard, calendar viewer)
- **ADMIN** - Full access (schedule management, version history)

## Styling

**Approach:** CSS Modules + global styles

**Component Styles:**
```typescript
import styles from './Dashboard.module.css';

<div className={styles.dashboardContainer}>
  <h1 className={styles.title}>Dashboard</h1>
</div>
```

**Benefits:**
- Scoped styles (no naming conflicts)
- Type-safe className references
- CSS co-located with components

**Global Styles:** `index.css`
- CSS variables for theming
- Reset/normalize styles
- Typography and layout utilities

## Testing

### Unit Tests
```typescript
import { render, screen } from '@testing-library/react';
import { Dashboard } from './Dashboard';

test('renders dashboard heading', () => {
  render(<Dashboard />);
  expect(screen.getByText(/dashboard/i)).toBeInTheDocument();
});
```

### Integration Tests
```typescript
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

test('fetches and displays schedules', async () => {
  const queryClient = new QueryClient();

  render(
    <QueryClientProvider client={queryClient}>
      <ScheduleList />
    </QueryClientProvider>
  );

  expect(await screen.findByText('Payroll Schedule')).toBeInTheDocument();
});
```

## Performance Optimizations

1. **Code Splitting** - Lazy load routes for faster initial load
2. **React Query Caching** - Minimize redundant API calls
3. **Memoization** - Use `useMemo` and `useCallback` for expensive computations
4. **Normalized API Responses** - Reduce payload sizes (calendar views)

## Browser Support

- Chrome/Edge (latest 2 versions)
- Firefox (latest 2 versions)
- Safari (latest 2 versions)
- No IE11 support (uses modern JavaScript)

## Future Enhancements

- Dark mode theme toggle
- Export calendar to iCal/CSV
- Advanced filtering and search
- Real-time updates via WebSocket
- Offline support with service workers
- Mobile-responsive views

## Deployment

The React build is integrated with the Spring Boot backend:

1. Build React app: `npm run build`
2. Output copied to Spring Boot `src/main/resources/static/`
3. Spring Boot serves React SPA on `/` route
4. API routes remain on `/api/v1/`

**Maven integration:**
```xml
<plugin>
  <groupId>com.github.eirslett</groupId>
  <artifactId>frontend-maven-plugin</artifactId>
  <executions>
    <execution>
      <id>npm install</id>
      <goals><goal>npm</goal></goals>
    </execution>
    <execution>
      <id>npm run build</id>
      <goals><goal>npm</goal></goals>
      <configuration>
        <arguments>run build</arguments>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Troubleshooting

**Issue:** API requests fail with CORS errors
**Solution:** Verify backend CORS configuration or use development proxy

**Issue:** "Cannot find module" errors
**Solution:** Run `npm install` to install dependencies

**Issue:** Build fails with TypeScript errors
**Solution:** Fix type errors or update type definitions

**Issue:** Stale data displayed
**Solution:** Check React Query cache settings or invalidate queries manually
