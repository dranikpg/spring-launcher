import React from 'react';
import {Form, FormField, Box, TextInput, Button, Meter, Heading, Stack, Spinner} from 'grommet';

import Launcher from './Launcher';
import Term from './Term';
import Login from './Login'

import './styles.css';
import 'xterm/css/xterm.css';


function App() {
  const [state, setState] = React.useState({
    authenticated: false,
    launchUrl: null
  });
  React.useEffect(async () => {
    try {
        let res = await fetch("/api/user")
        if (res.ok) setState({...state, authenticated: true})
    } catch (e) {}
  }, []);

  if (!state.authenticated) return <Login 
    authcb={() => setState({...state, authenticated: true})} />;
  else if (state.launchUrl == null) return <Launcher 
    launchcb={(url) => setState({...state, launchUrl: url})}/>
  else return <Term url={state.launchUrl} 
    returncb={() => setState({...state, launchUrl: null})}/>;
}

export default App;
