

function Card(props) {

    let className = "card";

    if (props.className !== undefined && props.className !== "") {
        className = "card " + props.className;
    }

    return (
        <article className={className}>
            {props.children}
        </article>
    );
}

export default Card;

