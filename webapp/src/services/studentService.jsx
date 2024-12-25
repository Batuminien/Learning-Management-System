import axios from "axios";
const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export const updateStudentInfo = async (ID, payload, accessToken) => {
    const response = await axios.put(
        `${BASE_URL}/api/v1/students/${ID}`,
        payload,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`,
                'Content-Type' : 'application/json',
            },
        }
    );
    return response;
}