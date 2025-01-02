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

export const getStudentCourses = async (studentID, accessToken) => {
  const response = await axios.get(
    `${BASE_URL}/api/v1/courses/student/${studentID}`,
    {
      headers : {
        Authorization : `Bearer ${accessToken}`,
      },
    }
  );
  return response.data;
}

export const getTeacherCourses = async (ID, accessToken) => {
  const response = await axios.get(
    `${BASE_URL}/api/v1/courses/teacher/${ID}`,
    {
      headers : {
        Authorization : `Bearer ${accessToken}`,
      },
    }
  );
  return response.data;
}

export const getCoursesGivenBy = async (userRole, accessToken, ID = null) => {
  if(userRole === 'ROLE_TEACHER') return await getTeacherCourses(ID, accessToken);
  else if(userRole === 'ROLE_ADMIN' || userRole === 'ROLE_COORDINATOR') return await getAllCourses(accessToken);
}