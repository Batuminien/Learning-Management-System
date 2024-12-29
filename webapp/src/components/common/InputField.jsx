
const InputField = ({type = 'text', label, placeholder, value, onChange, style, name, errorMessage = ''}) => {
    return(
        <div className="input-container">
            <label className='label'>{label}</label>
            <input  
                type={type}
                placeholder={placeholder}
                className="input"
                value={value}
                onChange={onChange}
                style={style}
                name={name}
            />
            {errorMessage && <p className='error-message'>{errorMessage}</p>}
        </div>
    );
}
export default InputField