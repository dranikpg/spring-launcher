#!/usr/bin/env python

import asyncio
import websockets

async def hello():
	async with websockets.connect("ws://localhost:8080/launch") as ws:
		pass
	print("done")

loop = asyncio.get_event_loop()
result = loop.run_until_complete(hello())

