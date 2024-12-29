import axios from "axios";
const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export const getAllClasses = async (accessToken) => {
  try {
    const response = await axios.get(
      `${BASE_URL}/api/v1/classes`,
      {
        headers : {
          Authorization : `Bearer ${accessToken}`
        }
      }
    );
    return response.data;
  }catch(error) {
    console.error("error fetching classes");
    throw error
  }
};

export const getTeacherClasses = async (accessToken) => {
    const response = await axios.get(
      `${BASE_URL}/api/v1/classes/teacher`,
      {
        headers : {
          Authorization : `Bearer ${accessToken}`
        },
      }
    );
    return response.data;
}

export const getClassByID = async (classID, accessToken) => {
  const response = await axios.get(
    `${BASE_URL}/api/v1/classes/${classID}`,
    {
      headers : {
        Authorization : `Bearer ${accessToken}`
      }
    }
  );
  return response;
}

export const getClasses = async (userRole, accessToken) => {
  if(userRole === 'ROLE_TEACHER') return await getTeacherClasses(accessToken);
  else if(userRole === 'ROLE_ADMIN' || userRole === 'ROLE_COORDINATOR') return await getAllClasses(accessToken);
}

export const getStudentClass = async (studentID, accessToken) => {
  const response = await axios.get(
    `${BASE_URL}/api/v1/classes/student/${studentID}`,
    {
      headers : {
        Authorization : `Bearer ${accessToken}`,
      },
    }
  );
  return response.data;
}


export const addStudent = async (studentID, classID, accessToken) => {
  const response = await axios.post(
    `${BASE_URL}/api/v1/classes/${classID}/students/${studentID}`,
    {},
    {
      headers : {
        Authorization : `Bearer ${accessToken}`
      }
    }
  );
  return response;
};
