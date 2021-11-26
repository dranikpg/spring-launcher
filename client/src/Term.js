import { Box, Button, Spinner, Stack } from 'grommet';
import React from 'react'

import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';


async function run(url, token, cancellation, failcb) {
    const term = new Terminal({ fontSize: 25 })
    term.setOption("theme", {
        'background': 'white',
        'foreground': 'black'
    })
    const fitAddon = new FitAddon();
    term.loadAddon(fitAddon);
    term.open(document.getElementById("term"))
    fitAddon.fit()
    term.writeln("[client] url: " + url)

    let ws = new WebSocket("ws://0.0.0.0:8080/launch")
    ws.onopen = () => {
        term.writeln("[client] connected")
        term.writeln("[client] sending authentication token")
        cancellation.then(() => ws.close())
        ws.send(token)
        ws.send(url)
    }
    ws.onclose = () => {
        term.writeln("Connection closed")
        setTimeout(failcb, 2000)
    }
    ws.onmessage = (msg) => term.writeln(msg.data)
}

function Term({url, returncb}) {
    let [token, setToken] = React.useState(null)

    React.useEffect(() => {
        if (!token) return;
        let cbs = []
        let cancelPromise = new Promise((resolve, reject) => cbs.push(resolve) )
        run(url, token, cancelPromise, returncb)
        return () => cbs[0]()
    }, [token]);

    React.useEffect(async () => {
        try {
            let recvtoken = await (await fetch("/launch")).text()
            console.log(recvtoken)
            setToken(recvtoken)
        } catch (e) {}
    }, [])

    if (!token) {
        return <Box direction="row" align="center" gap="medium" pad="small">
            <Spinner/>
            <b> Requesting launch token </b>
        </Box>
    }
    return <React.Fragment>
        <Box direction="row" align="center" gap="medium" pad="small">
            <Spinner/>
            <b> Running </b>
            <Button label="stop" onClick={() => returncb()} />
        </Box>
        <div id="term" style={{minWidth: "95vw", height:"95vh", paddingLeft: "10px"}}/>
    </React.Fragment>

}

export default Term;
