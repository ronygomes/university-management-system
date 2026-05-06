import { render, screen } from '@testing-library/react';
import ContentWrapper from './ContentWrapper';

describe('ContentWrapper', () => {
  it('renders the app bar with the system title', () => {
    render(<ContentWrapper>child</ContentWrapper>);

    expect(screen.getByText('University Management System')).toBeInTheDocument();
  });

  it('renders children', () => {
    render(<ContentWrapper><p>Test Child</p></ContentWrapper>);

    expect(screen.getByText('Test Child')).toBeInTheDocument();
  });
});
