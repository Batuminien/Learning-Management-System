import { useContext, useEffect, useState } from "react";
import { AuthContext } from "../../../../contexts/AuthContext";
import Loading from "../../../common/Loading/Loading";
import { getAnnouncementsByUser, markAsRead, markAsUnread } from "../../../../services/announcementService";
import Warning from "../../../common/IconComponents/Warning";
import NoResult from "../../../common/IconComponents/NoResult";
import SingleAnnouncement from "../SingleAnnouncement";

const OtherAnnouncements = () => {
    const { user } = useContext(AuthContext);
    const [loading, setLoading] = useState(false);
    const [loadError, setLoadError] = useState(false);
    const [announcements, setAnnouncements] = useState([]);

    useEffect(() => {
        const fetchAnnouncements = async () => {
            try{
                setLoading(true);
                const response = await getAnnouncementsByUser(user.accessToken);
                const allAnnouncements = response.data.data;
                const otherAnnouncements = allAnnouncements.filter(announcement => announcement.createdById !== user.id);
                setAnnouncements(otherAnnouncements);
            }catch(error){
                setLoadError(true);
            }finally{
                setLoading(false);
            }
        }
        fetchAnnouncements();
    }, []);

    const handleAnnouncementRead = async (id) => {
        try{
            setLoading(true);
            const response = await markAsRead(id, user.accessToken);
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

    const handleAnnouncementUnRead = async (id) => {
        try{
            setLoading(true);
            const response = await markAsUnread(id, user.accessToken);
            console.log(response);
            setAnnouncements((prevAnnouncements) => (
                prevAnnouncements.map((announcement) =>(
                    announcement.id === id ? {...announcement, read : false} : announcement
                ))
            ));
        }catch(error){
            console.log(error);
        }finally{
            setLoading(false);
        }
    }

    if(loading) return <Loading/>;
    if(loadError) return <Warning/>
    if(announcements.length === 0) return <NoResult/>;
    return(
        <>
            {announcements.map(announcement => (
                <SingleAnnouncement
                    key={announcement.id}
                    announcement={announcement}
                    onRead={handleAnnouncementRead}
                    onUnread={handleAnnouncementUnRead}
                />
            ))}
        </>
    );
}
export default OtherAnnouncements