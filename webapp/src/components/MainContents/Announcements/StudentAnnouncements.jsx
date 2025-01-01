import { useContext, useEffect, useState } from "react";
import { AuthContext } from "../../../contexts/AuthContext";
import { getStudentClass } from "../../../services/classesService";

import Loading from "../../common/Loading/Loading";
import Warning from "../../common/IconComponents/Warning";
import NoResult from '../../common/IconComponents/NoResult';

import { getAnnouncementsOf, markAsRead } from "../../../services/announcementService";
import { ClosedEnvelopeIcon, OpenEnvelopeIcon } from "../../../../public/icons/Icons";



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
                console.log(announcementResponse.data.data);
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
                        <div className="unit-container" key={announcement.id} style={{backgroundColor : announcement.read ? 'var(--read-announcement-background)' : ''}}>
                            <div className="unit-header" style={{cursor : 'text'}}>
                                <div className="">
                                    <p className='unit-section-title' style={{marginBottom : '8px'}}>{announcement.title}</p>
                                    <p>{announcement.content}</p>
                                </div>
                                <div onClick={() => {if(!announcement.read){handleAnnouncementRead(announcement.id)}}}>
                                    {announcement.read ? <OpenEnvelopeIcon/> : <ClosedEnvelopeIcon />}
                                </div>
                            </div>
                        </div>

                    ))
                )
            )}
        </>
    );
}
export default StudentAnnouncements;