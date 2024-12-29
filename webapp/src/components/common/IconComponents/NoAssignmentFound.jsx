import { SearchIcon } from "../../../../public/icons/Icons";
import './IconComponents.css';

const NoAssignmentFound = () => {
    return(
        <div className="icon-component-container">
            <SearchIcon/>
            <p className="icon-component-text">No Assignment Found</p>
        </div>
    );
}
export default NoAssignmentFound;