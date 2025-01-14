import { useEffect, useState } from 'react';
import  useAuth  from '../../../hooks/useAuth';
import { getAnnouncementsByUser, markAsRead } from '../../../services/announcementService';

import Loading from '../../common/Loading/Loading';
import NoResult from '../../common/IconComponents/NoResult';
import SingleAnnouncement from '../Announcements/SingleAnnouncement';

const AnnouncementSummary = () => {
    const { user } = useAuth();
    const [unreadAnnouncements, setUnreadAnnouncements] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const fetchAnnouncements = async () => {
            try{
                setLoading(true);
                const response = await getAnnouncementsByUser();
                const newAnnouncements = response.data.data.filter(announcement => (!announcement.read) && announcement.createdById !== user.id);
                console.log(newAnnouncements);
                setUnreadAnnouncements(newAnnouncements.reverse());
            }catch(error){
                console.log(error);
            }finally{
                setLoading(false);
            }
        }
        fetchAnnouncements(); 
    }, [user]);

    const handleRead = async (announcement) => {
        console.log(announcement);
        try{
            const response = await markAsRead(announcement);
            const updatedList = unreadAnnouncements.filter(announ => announ.id !== announcement);
            setUnreadAnnouncements(updatedList);
            console.log(response);
        }catch(error){
            console.log(error);
        }
    }

    if(loading) return <Loading/>
    if(unreadAnnouncements.length === 0) return <NoResult/>
    return(
        unreadAnnouncements.map(announcement => (
            <SingleAnnouncement key={announcement.id} announcement={announcement} onRead={handleRead}/>
        ))
    );
}
export default AnnouncementSummary;