import { useContext, useEffect, useState } from "react";
import Loading from "../../../common/Loading/Loading";
import { AuthContext } from "../../../../contexts/AuthContext";
import { getAnnouncementsByUser } from "../../../../services/announcementService";
import NoResult from '../../../common/IconComponents/NoResult';
import SingleAnnouncement from '../SingleAnnouncement';
import Warning from "../../../common/IconComponents/Warning";


const CreatedAnnouncements = () => {
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
                const ownAnnouncements = allAnnouncements.filter(announcement => announcement.createdById === user.id);
                setAnnouncements(ownAnnouncements);
            }catch(error){
                setLoadError(true);
            }finally{
                setLoading(false);
            }
        }
        fetchAnnouncements();
    }, []);

    if(loading) return <Loading/>;
    if(announcements.length === 0) return <NoResult/>;
    if(loadError) return <Warning/>;

    return(
        <>
            {announcements.map((announcement) => (
                <SingleAnnouncement
                    announcement={announcement}
                />
            ))}
        </>
    );
}
export default CreatedAnnouncements;