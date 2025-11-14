import { describe, it, expect } from "vitest";
import { MemoryRouter } from "react-router-dom";
import { AdminRoute } from "./AdminRoute";
import { AuthContext } from "../context/AuthContext";
import { render } from "@testing-library/react";

function renderWithAuth(ui, { user }) {
  return render(
    <AuthContext.Provider value={{ user }}>
      <MemoryRouter>{ui}</MemoryRouter>
    </AuthContext.Provider>
  );
}

describe("AdminRoute", () => {
  it("redirects to login if not authenticated", () => {
    const { container } = renderWithAuth(
      <AdminRoute>OK</AdminRoute>,
      { user: null }
    );
    expect(container.innerHTML).not.toContain("OK");
  });

  it("redirects to dashboard if not admin", () => {
    const user = { roles: ["ROLE_USER"] };
    const { container } = renderWithAuth(
      <AdminRoute>OK</AdminRoute>,
      { user }
    );
    expect(container.innerHTML).not.toContain("OK");
  });

  it("renders children if admin", () => {
    const user = { roles: ["ROLE_ADMIN"] };
    const { container } = renderWithAuth(
      <AdminRoute>OK</AdminRoute>,
      { user }
    );
    expect(container.innerHTML).toContain("OK");
  });
});
