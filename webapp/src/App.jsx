import React from "react";
import { AuthProvider } from "./contexts/AuthContext";
import AppRouter from "./routes/AppRouter";
import { ProfilePhotoProvider } from "./contexts/ProfilePhotoContext";

const App = () => {
  return(
    <AuthProvider>
      <ProfilePhotoProvider>
        <AppRouter />
      </ProfilePhotoProvider>
    </AuthProvider>
  );
}
export default App