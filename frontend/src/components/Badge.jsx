

function Badge(props) {

    let variant = props.variant;

    if (variant === undefined) {
        variant = "neutral";
    }

    return (
        <span className={"badge badge--" + variant}>
            {props.children}
        </span>
    );
}

export default Badge;
