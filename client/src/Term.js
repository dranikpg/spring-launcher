import { Box, Button, Spinner, Stack } from 'grommet';
import React from 'react'

import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';

async function run(url, cancellation) {
    const term = new Terminal({ fontSize: 25 })
    term.setOption("theme", {
        'background': 'white',
        'foreground': 'black'
    })
    const fitAddon = new FitAddon();
    term.loadAddon(fitAddon);
    term.open(document.getElementById("term"))
    fitAddon.fit()
    term.writeln("url: " + url)

    let ws = new WebSocket("ws://0.0.0.0:8080/launch")
    ws.onopen = () => {
        term.writeln("connected")
        cancellation.then(() => ws.close())
        ws.send(url)
    }
    ws.onmessage = (msg) => term.writeln(msg.data)
}

function Term({url, returncb}) {
    React.useEffect(() => {
        let cbs = []
        let cancelPromise = new Promise((resolve, reject) => cbs.push(resolve) )
        run(url, cancelPromise)
        return () => cbs[0]()
    });
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
