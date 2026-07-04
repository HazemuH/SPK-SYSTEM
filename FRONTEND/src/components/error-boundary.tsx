import { Component, type ErrorInfo, type ReactNode } from "react";
import { Button } from "@/components/ui/button";

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
}

/**
 * Catches render-time errors anywhere below it and shows a fallback instead of
 * a blank screen. Wrap the app with this. Hook a real logger into
 * `componentDidCatch` (e.g. Sentry) when you have one.
 */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("Uncaught render error:", error, info);
  }

  private handleReset = () => {
    this.setState({ hasError: false });
    window.location.assign("/");
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex min-h-screen flex-col items-center justify-center gap-4 p-6 text-center">
          <h1 className="text-xl font-semibold">Terjadi kesalahan</h1>
          <p className="text-sm text-muted-foreground">
            Maaf, ada yang tidak beres. Coba muat ulang halaman.
          </p>
          <Button onClick={this.handleReset}>Kembali ke Beranda</Button>
        </div>
      );
    }
    return this.props.children;
  }
}
