/**
 * Enumerated type for schedule run status.
 * Matches the backend RunStatus enum.
 */
export enum RunStatus {
  RUN = 'RUN',
  SKIP = 'SKIP',
  FORCE_RUN = 'FORCE_RUN',
  FORCE_SKIP = 'FORCE_SKIP'
}
