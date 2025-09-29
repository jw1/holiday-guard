/**
 * Defines the type for the raw schedule data coming from the backend API.
 */
export interface ScheduleResponseDto {
  id: string;
  name: string;
  description: string;
  country: string;
  active: boolean;
  createdAt: string; // ISO date string
  updatedAt: string;
  ruleType: string;
  ruleConfig: string;
}

/**
 * Defines the type for a schedule as used on the frontend,
 * with dates formatted for display.
 */
export interface Schedule {
  id: string; // UUID is a string
  name: string;
  description: string;
  country: string;
  active: boolean;
  createdAt: string; // Formatted date string
  ruleType: string;
  ruleConfig: string;
}
