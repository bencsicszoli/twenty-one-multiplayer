import { Navigate } from "react-router-dom";

export default function ProtectedRoute({ children }) {
  const token = localStorage.getItem("jwtToken");
  if (!token || token === "null" || token === "") {
    return <Navigate to="/" replace />;
  }
  return children;
}