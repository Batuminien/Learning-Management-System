import { useContext, useEffect, useState } from "react";
import { AuthContext } from "../../../contexts/AuthContext";
import { getStudentClass } from "../../../services/classesService";

import Loading from "../../common/Loading/Loading";
import Warning from "../../common/IconComponents/Warning";
import NoResult from '../../common/IconComponents/NoResult';

import { getAnnouncementsOf, markAsRead } from "../../../services/announcementService";
import SingleAnnouncement from "./SingleAnnouncement";



const StudentAnnouncements = () => {
    const { user } = useContext(AuthContext);

    const [loading, setLoading] = useState(false);
    const [fetchError, setFetchError] = useState(false);
    
    const [studentClass, setStudentClass] = useState({});
    const [announcements, setAnnouncements] = useState([]);

    useEffect(() => {
        const fetchClass = async () => {
            try{
                setLoading(true);
                const classResponse = await getStudentClass(user.id, user.accessToken);
                setStudentClass(classResponse.data);
                const announcementResponse = await getAnnouncementsOf(classResponse.data.id, user.accessToken);
                setAnnouncements(announcementResponse.data.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
            }catch(error){
                console.log(error);
                setFetchError(true);
            }finally{
                setLoading(false);
            }
        }
        fetchClass();
    }, []);
    
    const handleAnnouncementRead = async (id) => {
        console.log(`mark announcement ${id} as read`);
        try{
            setLoading(true);
            const response = await markAsRead(id, user.accessToken);
            console.log(response);
            setAnnouncements((prevAnnouncements) => (
                prevAnnouncements.map((announcement) =>(
                    announcement.id === id ? {...announcement, read : true} : announcement
                ))
            ));
        }catch(error){
            console.log(error);
        }finally{
            setLoading(false);
        }
    }

    if(fetchError) return <Warning/>

    return(
        <>
            {loading ? (
                <Loading/>
            ) : (
                announcements.length === 0 ? (
                    <NoResult/>
                ) : (
                    announcements.map(announcement => (
                        <SingleAnnouncement key={announcement.id} announcement={announcement} onRead={handleAnnouncementRead}/>
                    ))
                )
            )}
        </>
    );
}
export default StudentAnnouncements;