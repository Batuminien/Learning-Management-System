import { useEffect, useState } from "react";
import { getTimeFromDate } from "../../../utils/dateUtils";
import { ArrowDown, ArrowUp } from "../../../../public/icons/Icons";
import { ClosedEnvelopeIcon, OpenEnvelopeIcon } from "../../../../public/icons/Icons";



const SingleAnnouncement = ({announcement, onRead = null}) => {

    useEffect(() => {
        console.log(announcement);
    }, []);

    const [isExpanded, setIsExpanded] = useState(false);

    return(
        <div className="unit-container" style={{backgroundColor : announcement.read ? 'var(--read-announcement-background)' : ''}}>
            <div className="unit-header" onClick={() => setIsExpanded((prev) => !prev)}>
                <div className="unit-header-info">
                    <img src='https://placeholder.pics/svg/32x32' alt='icon' />
                    <span className="unit-subject">{announcement.title}</span>
                    <span className="unit-dueDate">{getTimeFromDate(announcement.createdAt)} - {new Date(announcement.createdAt).toLocaleDateString('en-GB')}</span>
                </div>
                <div style={{display : 'flex', alignItems : 'center',  columnGap : '6px'}}>
                    <div style={{display : 'grid', placeItems : 'center'}}
                        onClick={(event) => {
                            event.stopPropagation();
                            if (!announcement.read) {
                                onRead(announcement.id);
                            }
                        }}
                    >
                        {announcement.read ? <OpenEnvelopeIcon/> : <ClosedEnvelopeIcon />}
                    </div>
                    <button className="expand-btn">
                        {isExpanded ? <ArrowUp/> : <ArrowDown/>}
                    </button>
                </div>
            </div>
            {isExpanded && (
                <div >
                    <div style={{border : '1px solid grey', margin : '8px 0 12px 0'}}></div>
                        <span className="unit-subject">Açıklama : </span>
                        <span className="unit-section-text">
                            {announcement.content}
                        </span>
                    
                </div>
            )}
        </div>
    );
}
export default SingleAnnouncement;