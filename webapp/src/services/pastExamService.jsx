import axios from "axios"
const BASE_URL = import.meta.env.VITE_API_BASE_URL;
const EXAM_URL = BASE_URL + '/api/v1/past-exams';

export const getStudentExams = async (studentID) => {
    const response = await axios.get(
        `${EXAM_URL}/student/${studentID}`,
        {
            headers : {
                Authorization : `Bearer ${JSON.parse(sessionStorage.getItem('accessToken'))}`,
            },
        }
    );
    return response.data;
}

export const getAllExams = async () => {
    const response = await axios.get(
        `${EXAM_URL}`,
        {
            headers : {
                Authorization : `Bearer ${JSON.parse(sessionStorage.getItem('accessToken'))}`
            }
        }
    );
    return response.data;
}