# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.hostmanager.enabled = true
  config.hostmanager.manage_host = true
  config.vm.box = "centos/stream9"
  config.vm.box_version = "20250707.0"

  machines = [
   {
      # configuration de la VM postgresql
      name: "vm-postgresql",
      ip: "192.168.56.15",
      ram: 1024,
      cpu: 1,
      ports: [
        { guest: 5432, host: 15432, comment: "# accès SQL externe temporaire" }
      ],
      setup: "setup-postgresql.sh"
    },
    {
      # configuration de la VM rabbit
      name: "vm-rabbit",
      ip: "192.168.56.12",
      ram: 512,
      cpu: 1,
      ports: [
        { guest: 15672, host: 15672, comment: "# UI RabbitMQ" }
      ],
      setup: "setup-rabbitmq.sh"
    },
    {
      # configuration de la VM cache
      name: "vm-cache",
      ip: "192.168.56.13",
      ram: 512,
      cpu: 1,
      ports: [
        { guest: 11211, host: 11211, comment: "# test telnet memcached" }
      ],
      setup: "setup-memcache.sh"
    },
    {
      # configuration de la VM search
      name: "vm-search",
      ip: "192.168.56.14",
      ram: 2048,
      cpu: 2,
      ports: [
        { guest: 9200, host: 9200, comment: "# API Elasticsearch" }
      ],
      setup: "setup-elasticsearch.sh"
    },
    {
      # configuration de la VM nfs
      name: "vm-nfs",
      ip: "192.168.56.16",
      ram: 512,
      cpu: 1,
      ports: [],
      setup: "setup-nfs.sh"
    },
  
    # configuration de la VM userbackend
    {
      name: "vm-userbackend",
      ip: "192.168.56.11",
      ram: 4096,
      cpu: 2,
      ports: [
        { guest: 8080, host: 18080, comment: "# accès direct temporaire pour test" }
      ],
      setup: "setup-userbackend.sh"
    },
    {
    # configuration de la VM nginx
    name: "vm-nginx",
    ip: "192.168.56.10",
    ram: 512,
      cpu: 1,
      ports: [
        { guest: 80, host: 8080, comment: "# accès HTTP proxy web" }
      ],
      setup: "setup-nginx.sh"
    },
# configuration base de la VM
  ]

  machines.each do |machine|
    config.vm.define machine[:name] do |node|
      node.vm.hostname = machine[:name]
      node.vm.network "private_network", ip: machine[:ip]
      node.vm.network "public_network", bridge: "auto_detect"

      machine[:ports].each do |port|
        node.vm.network "forwarded_port", guest: port[:guest], host: port[:host]
        # #{port[:comment]}
        
      end

      node.vm.provider "virtualbox" do |vb|
        vb.memory = machine[:ram]
        vb.cpus = machine[:cpu]
      end

    # provisionnement de la VM
    node.vm.provision "shell", path: "provisioning/#{machine[:setup]}"
    end
  end
end
