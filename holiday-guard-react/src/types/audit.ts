/**
 * Data structure for an audit log entry from the backend API.
 */
export interface AuditLogDto {
  logId: number;
  scheduleId: number;
  scheduleName: string;
  versionId: number;
  queryDate: string; // Assuming YYYY-MM-DD
  shouldRunResult: boolean;
  reason: string;
  deviationApplied: boolean;
  clientIdentifier: string;
  createdAt: string; // ISO 8601 timestamp
}

/**
 * Type for the audit log data as displayed in the table,
 * with the createdAt property converted to a Date object.
 */
export interface AuditLog extends Omit<AuditLogDto, 'createdAt'> {
  createdAt: Date;
}
