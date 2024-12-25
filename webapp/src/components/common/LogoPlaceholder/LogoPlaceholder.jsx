import './LogoPlaceholder.css'

const LogoPlaceholder = () => {
    return(
        <div className='placeholder-container'>
            <img src='/icons/app_logo.png' alt="Logo image" className='logo' width="50px" style={{'backgroundColor' :'#14267C'} }/>
            <span className='title'>Learnovify</span>
        </div>
    );
}
export default LogoPlaceholder