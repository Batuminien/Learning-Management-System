import axios from "axios";
const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export const getAttendanceOfStudent = async (studentID, params, accessToken) => {
    const response = await axios.get(
        `${BASE_URL}/api/v1/attendance/student/${studentID}`,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`,
                'Content-Type' : 'application/json'
            },
            params,
        }
    );
    return response.data;
}

export const markAttendance = async (attendanceData, accessToken) => {
    const response = await axios.post(
        `${BASE_URL}/api/v1/attendance/mark`,
        attendanceData,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`,
                'Content-Type' : 'application/json',
            },
        }
    );
    return response;
}

export const updateAttendance = async (attendanceID, attendanceData, accessToken) => {
    const response = await axios.put(
        `${BASE_URL}/api/v1/attendance/${attendanceID}`,
        attendanceData,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`,
                'Content-Type' : 'application/json',
            },
        }
    );
    return response;
}

export const getAttendanceStatsOfStudent = async (studentID, classId, accessToken) => {
    const response = await axios.get(
        `${BASE_URL}/api/v1/attendance/stats/student/${studentID}`,
        {
            params : { classId },
            headers : {
                Authorization : `Bearer ${accessToken}`,
            },
        }
    );
    // console.log(response);
    return response.data;
}

export const getCourseAttendanceOfStudent = (courseID, params, accessToken) =>  {
    const response = axios.get(
        `${BASE_URL}/api/v1/attendance/stats/course/${courseID}`,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`,
            },
        },
        params
    );
    console.log(response);
    return response;
}