import { WarningIcon } from "../../../../public/icons/Icons";
import './IconComponents.css';

const Warning = () => {
    return(
        <div className='icon-component-container'>
            <WarningIcon/>
            <p className='icon-component-text'>HATA</p>
        </div>
    );
}
export default Warning;