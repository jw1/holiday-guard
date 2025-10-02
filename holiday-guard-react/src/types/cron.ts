export type DayOfMonthMode = 'every' | 'specific' | 'last';

export interface CronBuilderState {
    months: string[];           // ["1", "3", "5"] or ["*"]
    dayOfMonthMode: DayOfMonthMode;
    daysOfMonth: string[];      // ["1", "15"] or ["*"] or ["L"]
    daysOfWeek: string[];       // ["1", "2", "3", "4", "5"] or ["*"] or ["?"]
}

export interface CronExpressionBuilderProps {
    value: string;              // Full 6-field cron: "0 0 0 * * 1-5"
    onChange: (value: string) => void;
    onValidationChange: (isValid: boolean, error?: string) => void;
}
