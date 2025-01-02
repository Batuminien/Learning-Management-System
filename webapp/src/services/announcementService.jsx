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

export const getAnnouncementsOf = async (classID, accessToken) => {
    const response = await axios.get(
        `${announcementURL}/class/${classID}`,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`,
            },
        }
    );
    return response;
}

export const markAsRead = async (announcementID, accessToken) => {
    const response = await axios.post(
        `${announcementURL}/${announcementID}/read`,
        {},
        {
            headers : {
                Authorization : `Bearer ${accessToken}`,
            },
        }
    );
    return response;
}

export const markAsUnread = async (announcementID, accessToken) => {
    const response = await axios.post(
        `${announcementURL}/${announcementID}/unread`,
        {},
        {
            headers : {
                Authorization : `Bearer ${accessToken}`
            }
        }
    );
    return response;
}

export const getAnnouncementsByUser = async (accessToken) => {
    const response = await axios.get(
        `${announcementURL}/my-announcements`,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`
            }
        }
    );
    return response;
}

export const deleteAnnouncement = async (announcementID, accessToken) => {
    const response = await axios.delete(
        `${announcementURL}/${announcementID}`,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`
            }
        }
    );
    return response;
}

export const updateAnnouncement = async (announcementID, announcementData, accessToken) => {
    const response = await axios.put(
        `${announcementURL}/${announcementID}`,
        announcementData,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`
            }
        }
    );
    return response;
}