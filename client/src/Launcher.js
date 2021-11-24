import React, { useEffect } from 'react';
import {Box, Heading, TextInput, Button, Text, Table, TableRow, TableBody, TableCell, Spinner} from 'grommet';

function Row(k, v) {
    return <TableRow>
        <TableCell>
            <Text size="large">{k}</Text>
        </TableCell>
        <TableCell>
            <Text size="large" weight="bold">{v}</Text>
        </TableCell>
    </TableRow>
}

function SpringChecker({url}) {
    let [state, setState] = React.useState({
        valid: false,
        loading: true
    })
    useEffect(async () => {
        setState({...state, loading: true})
        let capture = url
        let rawUrl = url.replace("github.com", "raw.githubusercontent.com")
            .replace("tree", "") + "/build.gradle";
        let valid = false
        try {
            let resp = await fetch(rawUrl);
            let text = await resp.text()
            valid = text.includes("spring")
            valid &= url == capture
        } catch (e) {
            console.log(e)
        } finally {
            setState({valid: valid, loading: false})
        }
    }, [url])
    if (state.loading) {
        return "";
    } else if(state.valid) {
        return <Text> Looks like 🌱Spring🌱 👍 </Text>
    } else {
        return <Text> Sure this is Spring? 😕  </Text>
    }
}

function Launcher({launchcb}) {
    let [state, setState] = React.useState({
        repo: 'repo',
        folder: 'folder',
        branch: 'branch',
        valid: false,
        full: ''
    });
    let updateUrl = (url) => {
        let invalid = () => setState({valid: false});
        if (url.match(/^https?:/) == null) return invalid()
        let parts = url.split("/tree/")
        if (parts.length != 2) return invalid() 
        let repo = parts[0].match(/github\.com\/(.+?)\/(.+$)/i)
        let bf = parts[1].match(/(.*?)\/(.*)/)
        if (repo == null || bf == null) return invalid()
        setState({
            repo: repo[1] + '/' + repo[2],
            branch: bf[1],
            folder: bf[2],
            full: url,
            valid: true
        })
    };
    return (
        <Box fill align="center" justify="center">
          <Box width="large" align="stretch" gap="small">
            <Heading> Let's launch! 🚀 </Heading>
            <TextInput placeholder="URL"
                onChange={(e) => updateUrl(e.target.value)} />
            <Box direction="row" justify="end" style={{minHeight: "30px"}}>
                { state.valid ? <SpringChecker url={state.full} /> : <Text>&nbsp;</Text> }
            </Box>
            <Table>
                <TableBody>
                    {Row("Repo", state.repo)}
                    {Row("Folder", state.folder)}
                    {Row("Branch", state.branch)}
                </TableBody>
            </Table>
            <Button disabled={!state.valid} label="Launch"
                onClick={() => launchcb(state.full)}/>
          </Box>
        </Box>
    )
}

export default Launcher;
