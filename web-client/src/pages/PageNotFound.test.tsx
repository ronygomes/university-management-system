import { render, screen } from '@testing-library/react';
import PageNotFound from './PageNotFound';

describe('PageNotFound', () => {
  it('renders page not found message', () => {
    render(<PageNotFound />);

    expect(screen.getByText('Page Not Found')).toBeInTheDocument();
  });
});
