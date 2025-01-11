import axios from 'axios';
const BASE_URL = import.meta.env.VITE_API_BASE_URL;

const login = async (credentials) => {
  const response = await axios.post(`${BASE_URL}/api/v1/auth/login`, credentials);
  return response.data;
};

const register = async (credentials) => {
  const response = await axios.post(`${BASE_URL}/api/v1/auth/register`, credentials);
  return response.data;
}

const logout = async (accessToken, refreshToken = null) => {
  const response = await axios.post(
    `${BASE_URL}/api/v1/auth/logout`,
    {},
    {
      headers : {
        Authorization : `Bearer ${accessToken}`,
      }
    }
  );
  return response.data;
}
export default { login, logout, register };