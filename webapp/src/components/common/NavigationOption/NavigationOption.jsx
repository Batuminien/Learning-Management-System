import './NavigationOption.css'

function NavigationOption({title = 'Navigation item', IconSource, isHighlighted, onClick}) {
    return(
        <div className={`navigation-option ${isHighlighted ? 'highlighted' : 'default'}`} onClick={onClick}>
            {IconSource && <IconSource className={isHighlighted ? 'icon-highlighted' : 'icon'} size={34}/>}
            <p className="navigation-title">{title}</p>
        </div>
    );
}
export default NavigationOption