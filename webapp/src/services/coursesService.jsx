import axios from "axios";

export const getAllCourses = async (accessToken) => {
  const response = await axios.get(
    '/api/v1/courses',
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    }
  );
    return response.data;
};

// export const getAllCoursesbyClassID = async (accessToken, classID) => {
//   const response = await axios.get(
//     `/api/v1/courses/class/${classID}`,
//     {
//       headers : {
//         Authorization : `Bearer ${accessToken}`
//       }
//     }
//   );
//   return response;
// };

export const getAllSubjectsOf = async (accessToken, classID) => {
  try {
    const response = await axios.get(
      `/api/v1/courses/class/${classID}`,
      {
        headers : {
          Authorization : `Bearer ${accessToken}`
        }
      }
    );
    return response.data;
  }catch(error) {
    console.error("error fetching courses of class : ", classID)
  }
}