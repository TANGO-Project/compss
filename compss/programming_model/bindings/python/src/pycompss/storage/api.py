"""
@author: fconejer

Storage dummy connector
=======================
    This file contains the functions that any storage that wants to be used
    with PyCOMPSs must implement
    
    storage.api code example.
"""


def init(config_file_path=None, **kwargs):
    '''
    print "-----------------------------------------------------"
    print "| WARNING!!! - YOU ARE USING THE DUMMY STORAGE API. |"
    print "| Call to: init function.                           |"
    print "| Parameters: config_file_path = None"
    for key in kwargs:
        print "| Kwargs: Key %s - Value %s" % (key, kwargs[key])
    print "-----------------------------------------------------"
    '''
    pass


def finish(**kwargs):
    '''
    print "-----------------------------------------------------"
    print "| WARNING!!! - YOU ARE USING THE DUMMY STORAGE API. |"
    print "| Call to: finish function.                         |"
    for key in kwargs:
        print "| Kwargs: Key %s - Value %s" % (key, kwargs[key])
    print "-----------------------------------------------------"
    '''
    pass


def getByID(obj):
    """
    This functions retrieves an object from an external storage technology
    from the obj object.
    This dummy returns the same object as submited by the parameter obj.
    @param obj: key/object of the object to be retrieved.
    @return: the real object. 
    """
    print "-----------------------------------------------------"
    print "| WARNING!!! - YOU ARE USING THE DUMMY STORAGE API. |"
    print "| Call to: getByID                                  |"
    print "|   *********************************************   |"
    print "|   *** Check that you really want to use the ***   |"
    print "|   ************* dummy storage api *************   |"
    print "|   *********************************************   |"
    print "-----------------------------------------------------"
    return obj


class TaskContext(object):
    
    def __init__(self, logger, values, config_file_path=None, **kwargs):
        self.logger = logger
        self.values = values
        self.config_file_path = config_file_path
    
    def __enter__(self):
        # Do something prolog
    
        # Ready to start the task
        self.logger.info("Prolog finished")
        pass
    
    def __exit__(self, type, value, traceback):
        # Do something epilog
    
        # Finished
        self.logger.info("Epilog finished")
        pass


