
function getTemp()
pin = 4
ow.setup(pin)
count = 0
repeat
  count = count + 1
  addr = ow.reset_search(pin)
  addr = ow.search(pin)
  tmr.wdclr()
until((addr ~= nil) or (count > 100))
if (addr == nil) then
  print("No more addresses.")
else
  print(addr:byte(1,8))
  crc = ow.crc8(string.sub(addr,1,7))
  if (crc == addr:byte(8)) then
    if ((addr:byte(1) == 0x10) or (addr:byte(1) == 0x28)) then
      print("Device is a DS18S20 family device.")
        
          ow.reset(pin)
          ow.select(pin, addr)
          ow.write(pin, 0x44, 1)
          tmr.delay(1000000)
          present = ow.reset(pin)
          ow.select(pin, addr)
          ow.write(pin,0xBE,1)
          print("P="..present)  
          data = nil
          data = string.char(ow.read(pin))
          for i = 1, 8 do
            data = data .. string.char(ow.read(pin))
          end
          print(data:byte(1,9))
          crc = ow.crc8(string.sub(data,1,8))
          print("CRC="..crc)
          if (crc == data:byte(9)) then
             t = (data:byte(1) + data:byte(2) * 256) * 625
             t1 = t / 10000
             t2 = t % 10000
             return ("Temperature= "..t1.."."..t2.." Centigrade")
            
          end                   
          tmr.wdclr()
       
    else
      print("Device family is not recognized.")
    end
  else
    print("CRC is not valid!")
  end
end

end

function xmitTemp()
    local temp = 0

    temp = getTemp()
    if temp == -999999 then
        return
        end

    cu:send(tostring(temp))

    end -- xmitTemp

function initUDP()

    -- setup UDP port
    cu=net.createConnection(net.UDP)
    cu:connect(55056,"192.168.1.103")
	cu:send("initialisation complete")
	
	s=net.createServer(net.UDP)
	s:on("receive",function(s,c) xmitTemp() end)
	s:listen(5683)
	
    end -- initUDP
    
function initWIFI()
    print("Setting up WIFI...")

    wifi.setmode(wifi.STATION)

    wifi.sta.config("passwordistaco","robinson")
	cfg = {
    ip="192.168.1.112",
    netmask="255.255.255.0",
    gateway="192.168.1.1"
}
wifi.sta.setip(cfg)
    wifi.sta.connect()
    tmr.alarm(1, 1000, 1,   
        function() 
            if wifi.sta.getip()== nil then 
                print("IP unavailable, Waiting...") 
            else 
                tmr.stop(1)
                print("Config done, IP is "..wifi.sta.getip())
                end 
            end -- function
        )
    end -- initWIFI

initWIFI()
initUDP()
