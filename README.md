<h1 align="center">
  <br>
  <a href="https://github.com/LimeIncOfficial/Stealth-Craft"><img src="https://github.com/LimeIncOfficial/Stealth-Craft/blob/main/assets/logo.png" width="150px" alt="Logo"></a>
  <br>
  Stealth Craft
  <br>
</h1>

# What is It 🤔

#### A look into how common digital infastructure can be hijacked and used to mask malicious activity. Stealth Craft is still a 100% functional minecraft server (based on Minestom) that acts as a redirector for implant traffic to your desired c2 server. It also acts as a standalone c2 server. It can also be used as a standalone reverse proxy when configured correctly.

<h4 align="center">
<a href="https://github.com/LimeIncOfficial/Stealth-Craft"><img src="https://github.com/LimeIncOfficial/Stealth-Craft/blob/main/assets/Diagram.drawio.png" width="500px" alt="diagram"></a>

#### Keep in mind that Stealth Craft was written with the intent of having the c2 server being binded to locally. Directly supplying traffic to and from within the same machine. I might consider adding the option in the fututre to forward traffic externally, but for right now you will have to run a one server solution. 
  
</h4>

# How Does it Work ⛩️
  #### 
  1. Server initializes but with a special added protocol (default `666`) & another (default `210`). The latter is if you want to use the mc server as the c2 which requires your payload to have a mc client with the correct version number (`1.17.1`). 
  2. When implant initiates handshake with server it recognizes your chosen protocol. Regular players use 756 to connect.
  3. If `666` or `IMPLANT_PROTOCOL` defined in `MinecraftServer.java` the server forwards to your local c2 default `4444`
  4. Else if protocol `210` is used or `Stealth_C2_PROTOCOL` the implant connects to the game server directly and isn't forwarded. Spawning as Spectator in the world, constantly reading chat for `%%%[Command Parameter]` 
 
# What to do 🛠️
  ####
  1. Setup gameserver as you like. Follow a Minestom server setup guide or just write it yourself.
  2. Bind your c2 to `localhost` at `4444` (Not needed if using secondary protocol)
  3. Forward Game Server (You can use ngrok for this to make it easy)
  4. Configure Implant to connect to your ngrok or your chosen forwarding solution
  5. Wait and Profit!

# Why so much setup 🤡
  ####
  This isn't something you clone and have running in 30 sec. Add this your arsenal for when your infastructure needs something totally out of left field. I encourage you to modyify it, both the implant and the server so that it can adapt to your engagement. Hopefully this POC demonstrates the vulnerability of legitamate service modyifcation and masking implant traffic using unsual means.
