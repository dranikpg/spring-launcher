import React from 'react';
import {Box, Heading, TextInput, Button} from 'grommet';

function Login({authcb}) {
    const [state, setState] = React.useState({
        password: "",
        busy: false,
    })
    const tryAuth = async () => {
        setState({busy: true})
        try {
            let res = await fetch("/api/login", {
                method: 'POST',
                body: JSON.stringify({password: state.password}),
				headers: { "Content-Type": "application/json" }
            });
            console.log(res)
            if (res.ok) authcb();
        } catch (e) {
            console.log(e)
        }
        setState({busy: false, password: ""})
    };

    return (
      <Box fill align="center" justify="center">
        <Box width="medium" align="center">
          <div style={{width: "200px", height:"200px", overflow:"hidden", borderRadius:"100px"}}>
            <img 
              style={{width: "200px"}}
              src="https://w0.peakpx.com/wallpaper/457/43/HD-wallpaper-cat-police-meme-catpolice.jpg"/>
          </div>
          <Heading> Authenticate </Heading>
          <TextInput
            type='password'
            value={state.password}
            onChange= { e => setState({...state, password: e.target.value}) }
          />
          <Button style={{marginTop: "20px"}} label="Login" size="large"
            onClick={tryAuth} disabled={state.busy}/>
        </Box>
      </Box>
    );
}

export default Login;
