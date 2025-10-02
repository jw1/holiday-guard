import { describe, it, expect } from 'vitest';
import {
    compressToRangesAndLists,
    expandCronField,
    parseCronToState,
    stateToCron,
    cronToHumanReadable
} from '../cronUtils';

describe('cronUtils', () => {
    describe('compressToRangesAndLists', () => {
        it('should compress consecutive numbers to ranges', () => {
            expect(compressToRangesAndLists([1, 2, 3, 4, 5])).toBe('1-5');
        });

        it('should keep non-consecutive numbers as lists', () => {
            expect(compressToRangesAndLists([1, 3, 5, 7])).toBe('1,3,5,7');
        });

        it('should mix ranges and lists', () => {
            expect(compressToRangesAndLists([1, 2, 3, 10, 11, 12])).toBe('1-3,10-12');
        });

        it('should handle single value', () => {
            expect(compressToRangesAndLists([5])).toBe('5');
        });

        it('should handle empty array', () => {
            expect(compressToRangesAndLists([])).toBe('*');
        });
    });

    describe('expandCronField', () => {
        it('should expand ranges', () => {
            expect(expandCronField('1-5', 1, 7)).toEqual([1, 2, 3, 4, 5]);
        });

        it('should expand lists', () => {
            expect(expandCronField('1,3,5', 1, 7)).toEqual([1, 3, 5]);
        });

        it('should handle wildcards', () => {
            expect(expandCronField('*', 1, 7)).toEqual([]);
        });

        it('should handle mixed ranges and lists', () => {
            expect(expandCronField('1-3,5,7', 1, 7)).toEqual([1, 2, 3, 5, 7]);
        });
    });

    describe('parseCronToState and stateToCron', () => {
        it('should parse every day cron', () => {
            const state = parseCronToState('0 0 0 * * *');
            expect(state.dayOfMonthMode).toBe('every');
            expect(state.daysOfWeek).toContain('*');
        });

        it('should parse weekdays cron', () => {
            const state = parseCronToState('0 0 0 * * 1-5');
            expect(state.dayOfMonthMode).toBe('every');
            expect(state.daysOfWeek).toEqual(['1', '2', '3', '4', '5']);
        });

        it('should parse specific days of month', () => {
            const state = parseCronToState('0 0 0 1,15 * ?');
            expect(state.dayOfMonthMode).toBe('specific');
            expect(state.daysOfMonth).toEqual(['1', '15']);
        });

        it('should parse last day of month', () => {
            const state = parseCronToState('0 0 0 L * ?');
            expect(state.dayOfMonthMode).toBe('last');
            expect(state.daysOfMonth).toEqual(['L']);
        });

        it('should round-trip conversion', () => {
            const original = '0 0 0 * * 1-5';
            const state = parseCronToState(original);
            const result = stateToCron(state);
            expect(result).toBe(original);
        });
    });

    describe('cronToHumanReadable', () => {
        it('should describe every day', () => {
            const state = parseCronToState('0 0 0 * * *');
            expect(cronToHumanReadable(state)).toBe('Every day');
        });

        it('should describe weekdays', () => {
            const state = parseCronToState('0 0 0 * * 1-5');
            expect(cronToHumanReadable(state)).toContain('Weekdays');
        });

        it('should describe specific months', () => {
            const state = parseCronToState('0 0 0 * 1,3,5 1-5');
            expect(cronToHumanReadable(state)).toContain('Jan, Mar, May');
        });

        it('should describe last day of month', () => {
            const state = parseCronToState('0 0 0 L * ?');
            expect(cronToHumanReadable(state)).toBe('Last day of month');
        });
    });
});
