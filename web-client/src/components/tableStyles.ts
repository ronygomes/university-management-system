import type { SxProps, Theme } from '@mui/material';

export const DEFAULT_ROWS_PER_PAGE = 10;

export const ROWS_PER_PAGE_OPTIONS = [5, 10, 25];

export const tableHeadRowSx: SxProps<Theme> = {
  '& th': {
    backgroundColor: 'grey.200',
    color: 'text.primary',
    fontWeight: 600,
  },
};
