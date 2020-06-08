"""Custom topology SDN-Labor 2
Fehlertolerante Architektur mit 3 ringfoermig verbundenen Switches
"""

host_per_switch_count = 3

from mininet.topo import Topo

class MyTopo( Topo ):
	"Labor 2 topology"
	
	def __init__( self ):
		# Initialize topology
		Topo.__init__( self )
		
		# Add switches
		s1 = self.addSwitch( 'S1')
		s2 = self.addSwitch( 'S2' )
		s3 = self.addSwitch( 'S3' )
		
		# Add links between switches together with their specific ports
		self.addLink (s1, s2, 2, 1)
		self.addLink (s2, s3, 3, 2)
		self.addLink (s3, s1, 1, 3)
		
		# Add hosts:
		for i in range (1,4):
			switchstring = 'S%d' %i
			for j in range (1, host_per_switch_count+1):
				hoststring = 'H%d%d' % (i, j)
				self.addHost(hoststring)
				self.addLink (hoststring, switchstring, 1, j+10)


topos = { 'mytopo': ( lambda: MyTopo() ) }
