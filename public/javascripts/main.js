/** @jsx React.DOM */

var Player = React.createClass({
    render: function() {
        return (
            <div className="player">
                <span className="player-name">{this.props.player.name}</span>
                <span> : </span>
                <span className="player-points">{this.props.player.points} pts</span>
            </div>
        );
    }
});

var Log = React.createClass({
    showDate : function (dateStr) {
        var date = moment(dateStr, "SS");
        return date.calendar();
    },
    showApp : function (app) {
        var nameOfKey = app.name || app.key;
        if (app.url) {
            return <a href="@url">{nameOfKey}</a>;
        } else {
            return nameOfKey;
        }
    },
    showPoints: function (points) {
        if (points !== 0) {
            (points > 0 ? '+' : '') + points + " points"
        }
    },
    render: function() {
        return (
            <div className="log">
                <span> {this.showDate(this.props.log.date)} </span>
                <span> : </span>
                <span> {this.props.log.player && this.props.log.player.name} </span>
                <span> {this.props.log.msg} </span>
                <span> {this.props.log.app && this.showApp(this.props.log.app)} </span>
                <span> {this.showPoints(this.props.log.diff)} </span>
                <span> :: </span>
                <span> {this.props.log.oldPoints} </span>
                <span> ⇒ </span>
                <span> {this.props.log.newPoints} </span>
            </div>
        );
    }
});

$("[data-player]").each(function () {
    var id = this.dataset.player;
    var infos = window.pageInfos.players[id];
    React.renderComponent(<Player player={infos} />, this);
});

$("[data-log]").each(function () {
    var id = this.dataset.log;
    var infos = window.pageInfos.logs[id];
    React.renderComponent(<Log log={infos} />, this);
});
