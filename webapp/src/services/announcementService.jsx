import axios from "axios";
const BASE_URL = import.meta.env.VITE_API_BASE_URL;

const announcementURL = BASE_URL + '/api/v1/announcements'

export const createAnnouncement = async (announcementData, accessToken) => {
    const response =  await axios.post(
        announcementURL,
        announcementData,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`
            }
        }
    );
    return response;
}