import axios from "axios";
const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export const getAllCourses = async (accessToken) => {
  const response = await axios.get(
    `${BASE_URL}/api/v1/courses`,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    }
  );
    return response.data;
};

export const getAllSubjectsOf = async (classID, accessToken) => {
  const response = await axios.get(
    `${BASE_URL}/api/v1/courses/class/${classID}`,
    {
      headers : {
        Authorization : `Bearer ${accessToken}`
      }
    }
  );
  return response.data;
}